package org.lucas.arbackend.service.payment;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.OrganisationSubscription;
import org.lucas.arbackend.entity.Organisation.SubscriptionPlan;
import org.lucas.arbackend.entity.PlanTypes;
import org.lucas.arbackend.entity.payment.PaymentLog;
import org.lucas.arbackend.exception.InvalidPlanException;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.payment.PaymentLogRepository;
import org.lucas.arbackend.service.cache.CacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
// TODO: Signup to payFast for an account to test
// https://developers.payfast.co.za/docs#home
@Service
@Slf4j
@RequiredArgsConstructor
public class PayFastSubscriptionService {

    private final OrganisationRepository orgRepo;
    private final PaymentLogRepository logRepo;
    private final CacheService cacheService;

    @Value("${payfast.pass-phrase}")
    private String passphrase;

/**
 * Processes an IPN (Instant Payment Notification) from PayFast payment gateway.
 * This method handles the entire payment verification and processing workflow.
 *
 * @param params A map containing all payment parameters received from PayFast
 * @throws SecurityException If the signature validation fails
 * @throws EntityNotFoundException If the organization is not found
 */
    @Transactional
    public void processIpn(Map<String, String> params) {
        // 1. Signature Check - Verify the payment authenticity using PayFast signature
        if (passphrase.isBlank()) {
            throw new RuntimeException("No passphrase provided for PayFast signature validation, check yaml");
        }

        if (!isSignatureValid(params, passphrase)) {
            throw new SecurityException("Invalid PayFast signature detected");
        }

        // 2. Status Check - Only process completed payments, ignore all other statuses
        if (!"COMPLETE".equalsIgnoreCase(params.get("payment_status"))) {
            log.info("Ignored IPN: Status is {}", params.get("payment_status"));
            return;
        }

        // 3. Idempotency Check - Prevent duplicate processing of the same payment
        String pfId = params.get("pf_payment_id");
        if (logRepo.existByPdPaymentId(pfId)) {
            log.info("Payment {} already processed.", pfId);
            return;
        }

        // 4. Validate Amount & Identity - Ensure payment amount matches organization's plan
        Long orgId = Long.parseLong(params.get("custom_int1"));
        double gross = Double.parseDouble(params.get("amount_gross"));

    // Retrieve organization from database, throw exception if not found
        Organisation org = orgRepo.findById(orgId)
            .orElseThrow(() -> new EntityNotFoundException("Org not found"));

        // Logic: Verify 'gross' matches your Plan Price before renewing
        updateSubscription(org, params.get("item_name"));

        // 5. Audit Log - Create a permanent record of the successful payment
        logRepo.save(PaymentLog.builder()
                .pfPaymentId(pfId)        // Store PayFast payment ID for reference
                .orgId(orgId)             // Link payment to organization
                .amount(BigDecimal.valueOf(gross))  // Store payment amount
                .paymentStatus("SUCCESS")  // Mark payment as successful
                .rawData(params.toString())// Store raw payment data for auditing
                .build());

        // 6. Redis Sync - Update Redis cache with latest organization data
        cacheService.updateCache("auth_user", org.getEmail(), org);
    }

/**
 * Updates the subscription for an organization by extending its end date by one month
 * and setting the status to active (1).
 *
 * @param org The organization whose subscription needs to be updated
 */
    private void updateSubscription(Organisation org, String subscriptionPlan) {

        if (subscriptionPlan.isBlank()) {
            throw new RuntimeException("No subscription type provided");  // Validate that subscription plan is provided
        }
    // Get the organization's current subscription
        OrganisationSubscription sub = org.getSubscription();  // Retrieve the existing subscription object

        PlanTypes purchasedPlan;
        try {
            // Convert the subscription plan string to enum value (case-insensitive)
            purchasedPlan = PlanTypes.valueOf(subscriptionPlan.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Handle invalid subscription plan by throwing a custom exception
            throw new InvalidPlanException("Invalid subscription plan: " + subscriptionPlan);
        }
    // Determine the base date: use current time if subscription has ended or never existed,
    // otherwise use the existing end date
        LocalDateTime base = (sub.getEndedAt() == null || sub.getEndedAt().isBefore(LocalDateTime.now()))
                             ? LocalDateTime.now() : sub.getEndedAt();  // Set the starting point for subscription extension

        switch (purchasedPlan) {
            case MONTHLY ->  // For monthly plan, extend subscription by 1 month
                sub.setEndedAt(base.plusMonths(1));
            case YEARLY ->  // For yearly plan, extend subscription by 1 year
                sub.setEndedAt(base.plusYears(1));
        }
        sub.setStatus(1);  // Set subscription status to active (1)

    // Save the updated organization to the repository
        orgRepo.save(org);  // Persist the changes to the database
    }

/**
 * Validates the signature of parameters received from PayFast ITN (Instant Transaction Notification)
 * by comparing the calculated MD5 hash with the received signature.
 *
 * @param params A map containing all parameters received from PayFast
 * @param passphrase The passphrase used for additional security (if any)
 * @return true if the signature is valid, false otherwise
 */
    private static boolean isSignatureValid(Map<String, String> params, String passphrase) {
    // Get the signature received from PayFast
        String receivedSignature = params.get("signature");
    // If no signature is present, validation fails immediately
        if (receivedSignature == null) return false;

    // StringBuilder to construct the base string for hashing
        StringBuilder sb = new StringBuilder();

        // 1. Concatenate in the EXACT order received (PayFast ITN order)
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!entry.getKey().equals("signature") && entry.getValue() != null && !entry.getValue().isEmpty()) {
                sb.append(entry.getKey()).append("=")
                  .append(payFastEncode(entry.getValue().trim()))
                  .append("&");
            }
        }

        // 2. Remove trailing & and add passphrase
        String baseString = sb.substring(0, sb.length() - 1);
            baseString += "&passphrase=" + payFastEncode(passphrase.trim());

        // 3. MD5 Hash
        return md5(baseString).equalsIgnoreCase(receivedSignature);
    }

/**
 * Encodes a string according to PayFast's specific requirements:
 * - Spaces should be encoded as '+'
 * - Hexadecimal characters should be in uppercase (e.g. %2F)
 *
 * @param value The string to be encoded
 * @return The encoded string following PayFast's specifications
 */
    private static String payFastEncode(String value) {
        // PayFast requires: Spaces as '+', Uppercase Hex (e.g. %2F)
        String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8)
                .replace("%20", "+");

    // Compile a pattern to match hexadecimal sequences in the encoded string
        Pattern pattern = Pattern.compile("%([0-9a-f]{2})");
    // Create a matcher to find the pattern in the encoded string
        Matcher matcher = pattern.matcher(encoded);

    // Replace all matches with their uppercase equivalents
        return matcher.replaceAll(m -> m.group().toUpperCase());
    }

/**
 * Computes the MD5 hash of the input string.
 *
 * @param input The string to be hashed
 * @return The MD5 hash of the input string as a hexadecimal value
 */
    private static String md5(String input) {
        try {
            // Create a MessageDigest instance for MD5 algorithm
            MessageDigest md = MessageDigest.getInstance("MD5");
            // Compute the hash of the input string using UTF-8 encoding
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            // Create a StringBuilder to store the hexadecimal representation
            StringBuilder sb = new StringBuilder();
            // Convert each byte in the hash to a two-digit hexadecimal value
            for (byte b : hash) sb.append(String.format("%02x", b));
            // Return the hexadecimal string representation of the hash
            return sb.toString();
        } catch (Exception e) { throw new RuntimeException(e); }
    }
}
