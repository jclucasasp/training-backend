package org.lucas.arbackend.service.payment;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.OrganisationSubscription;
import org.lucas.arbackend.entity.Organisation.SubscriptionPlan;
import org.lucas.arbackend.entity.PlanTypes;
import org.lucas.arbackend.entity.payment.PaymentLog;
import org.lucas.arbackend.exception.InvalidPlanException;
import org.lucas.arbackend.mapper.OrganisationMapper;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.SubscriptionPlanRepository;
import org.lucas.arbackend.repository.payment.PaymentLogRepository;
import org.lucas.arbackend.service.cache.CacheService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayFastSubscriptionService {

    private final OrganisationRepository orgRepo;
    private final PaymentLogRepository logRepo;
    private final OrganisationMapper orgMapper;
    private final CacheService cacheService;
    private final SubscriptionPlanRepository subPlanRepo;
    private final RestTemplate restTemplate = new RestTemplate(); // Ideally injected via Config

    @Value("${payfast.pass-phrase}")
    private String passphrase;

    @Value("${payfast.merchant-id}")
    private String merchantId;

    private static final String API_BASE_URL = "https://api.payfast.co.za/subscriptions/";

    @Transactional
    public void processIpn(Map<String, String> params) {
        if (passphrase == null || passphrase.isBlank()) {
            throw new RuntimeException("No passphrase provided for PayFast signature validation");
        }

        // FIX: Compare the received signature with the calculated one
        String receivedSignature = params.get("signature");
        if (receivedSignature == null || !receivedSignature.equals(calculateItnSignature(params, passphrase))) {
            throw new SecurityException("Invalid PayFast signature detected");
        }

        if (!"COMPLETE".equalsIgnoreCase(params.get("payment_status"))) {
            log.info("Ignored IPN: Status is {}", params.get("payment_status"));
            return;
        }

        String pfId = params.get("pf_payment_id");
        if (logRepo.existsByPfPaymentId(pfId)) {
            log.info("Payment {} already processed.", pfId);
            return;
        }

        Long orgId = Long.parseLong(params.get("custom_int1"));
        double gross = Double.parseDouble(params.get("amount_gross"));

        Organisation org = orgRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Org not found"));

        updateSubscription(org, params.get("item_name"));

        logRepo.save(PaymentLog.builder()
                .pfPaymentId(pfId)
                .orgId(orgId)
                .amount(BigDecimal.valueOf(gross))
                .paymentStatus("SUCCESS")
                .rawData(params.toString())
                .build());

        cacheService.updateCache("auth_user", org.getEmail(), orgMapper.mapToOrgResponse(org));
    }

    /**
     * Calls the PayFast API to fetch the current status of a recurring subscription.
     */
    public String fetchSubscriptionStatus(String token) {
        String url = API_BASE_URL + token + "/fetch";

        HttpHeaders headers = new HttpHeaders();
        // Generate the 4 required headers for the REST API
        Map<String, String> apiHeaders = generateApiHeaders(Map.of());

        apiHeaders.forEach(headers::set);

        HttpEntity<String> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, entity, String.class).getBody();
    }

    /**
     * Generates headers using the alphabetical sort required by the API.
     */
    private Map<String, String> generateApiHeaders(Map<String, String> additionalParams) {
        String timestamp = ZonedDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // 1. Sort ALL variables (including passphrase) alphabetically
        TreeMap<String, String> sortedMap = new TreeMap<>();
        sortedMap.put("merchant-id", merchantId);
        sortedMap.put("version", "v1");
        sortedMap.put("timestamp", timestamp);
        if (passphrase != null) sortedMap.put("passphrase", passphrase.trim());
        sortedMap.putAll(additionalParams);

        // 2. Concatenate into a base string
        String baseString = sortedMap.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isBlank())
                .map(entry -> entry.getKey() + "=" + payFastEncode(entry.getValue()))
                .collect(Collectors.joining("&"));

        return Map.of(
                "merchant-id", merchantId,
                "version", "v1",
                "timestamp", timestamp,
                "signature", md5(baseString)
        );
    }


/**
 * Calculates the ITN (Instant Transaction Notification) signature based on the provided data and passphrase.
 * This method constructs a string from the map entries (excluding the signature itself),
 * encodes the values, appends the passphrase if provided, and then calculates the MD5 hash.
 *
 * @param data A map containing key-value pairs of transaction data
 * @param passPhrase An optional passphrase to include in the signature calculation
 * @return The calculated MD5 hash as a string
 */
    private String calculateItnSignature(Map<String, String> data, String passPhrase) {
    // StringBuilder to efficiently construct the string for hash calculation
        StringBuilder sb = new StringBuilder();

    // Iterate through all entries in the data map
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

        // Skip the signature key itself as it shouldn't be included in the calculation
            if (!key.equals("signature")) {
            // Append key, encoded value, and separator to the string
                sb.append(key).append("=").append(payFastEncode(value)).append("&");
            }
        }

    // Remove the trailing ampersand from the constructed string
        String getString = sb.substring(0, sb.length() - 1);

    // Append the passphrase if it's provided and not blank
        if (passPhrase != null && !passPhrase.isBlank()) {
        getString += "&passphrase=" + payFastEncode(passPhrase.trim());
    }

    // Calculate the MD5 hash of the constructed string
    String calculatedHash = md5(getString);
    // Retrieve the signature from the original data for comparison
    String receivedSignature = data.get("signature");

    // Log the base string used for calculation and the comparison results
    log.info("ITN Base String: [{}]", getString);
    log.info("Calculated: {} | Received: {}", calculatedHash, receivedSignature);

    return calculatedHash;
    }

/**
 * Encodes a string using URL encoding with specific modifications
 * Replaces spaces with '+' and converts all hexadecimal codes to uppercase
 *
 * @param value The string to be encoded
 * @return The encoded string with modified format
 */
    private String payFastEncode(String value) {
    // URL encode the value using UTF-8 charset and replace %20 with +
        String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8).replace("%20", "+");
    // Compile a regex pattern to match percent-encoded characters (e.g., %2F, %3A)
        Pattern pattern = Pattern.compile("%[0-9a-f]{2}");
    // Create a matcher to find all matches of the pattern in the encoded string
        Matcher matcher = pattern.matcher(encoded);
    // Replace all matches with their uppercase equivalents (e.g., %2f becomes %2F)
        return matcher.replaceAll(m -> m.group().toUpperCase());
    }


/**
 * Computes the MD5 hash of the input string.
 * MD5 is a widely used cryptographic hash function that produces a 128-bit hash value.
 *
 * @param input The string to be hashed
 * @return The MD5 hash of the input string as a hexadecimal value
 * @throws RuntimeException if MD5 algorithm is not available or an error occurs during hashing
 */
    private String md5(String input) {
        try {
        // Get MD5 digest instance
            MessageDigest md = MessageDigest.getInstance("MD5");
        // Compute the hash in bytes
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
        // Convert the byte array into a hexadecimal string
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
// TODO: Need to check at month end if the recurring payment was made and update as necessary
/**
 * Updates the subscription for an organization based on the provided subscription term.
 * This method handles both new subscriptions and renewals of existing ones.
 *
 * @param org The organization whose subscription needs to be updated
 * @param subTerm The subscription term (e.g., "MONTHLY" or "YEARLY")
 * @throws RuntimeException If no subscription type is provided
 */
    private void updateSubscription(Organisation org, String subTerm) {
    // Validate that subscription term is provided
        if (subTerm.isBlank()) throw new RuntimeException("No subscription type provided");

    // Convert the subscription term to the corresponding PlanTypes enum value
        PlanTypes purchasedPlan = PlanTypes.valueOf(subTerm.toUpperCase());
    // Get the organization's current subscription
        OrganisationSubscription sub = org.getSubscription();
    // Find the subscription plan in the repository based on the purchased plan
        SubscriptionPlan subPlan = subPlanRepo.findByPlan(purchasedPlan);

    // Check if organization already has a subscription
        if (sub != null) {
        // Log debug information for existing subscription
            log.info("DEBUG: Organisation found for [{}] with subscription: {}", org.getEmail(), sub);
        } else {
        // Create new subscription if none exists
            sub = new OrganisationSubscription();
            sub.setOrganisation(org);
            org.setSubscription(sub);
        // Log debug information for new subscription creation
            log.info("DEBUG: Organisation found for [{}] with no subscription, creating new one", org.getEmail());
        }

    // Determine the base date for subscription calculation
    // Use current time if subscription is null or ended, otherwise use the existing end date
        LocalDateTime base = (sub.getEndedAt() == null || sub.getEndedAt().isBefore(LocalDateTime.now()))
                ? LocalDateTime.now() : sub.getEndedAt();
    // Update the subscription end date based on the purchased plan
        switch (purchasedPlan) {
            case MONTHLY -> sub.setEndedAt(base.plusMonths(1));  // Add 1 month for monthly plan
            case YEARLY -> sub.setEndedAt(base.plusYears(1));    // Add 1 year for yearly plan
        }

    // Set the subscription plan and status
        sub.setSubscriptionPlan(subPlan);
        sub.setStatus(1);
    // Save the updated organization to the repository
        orgRepo.save(org);
    }
}