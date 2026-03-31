package org.lucas.arbackend.service.payment;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.InternalException;
import org.lucas.arbackend.dto.payfast.PayFastSubscriptionDto;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.OrganisationSubscription;
import org.lucas.arbackend.entity.Organisation.SubscriptionPlan;
import org.lucas.arbackend.entity.PlanTypes;
import org.lucas.arbackend.entity.payment.FailureCode;
import org.lucas.arbackend.entity.payment.PaymentLog;
import org.lucas.arbackend.entity.payment.PaymentStatus;
import org.lucas.arbackend.entity.payment.SubscriptionStatus;
import org.lucas.arbackend.mapper.OrganisationMapper;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.SubscriptionPlanRepository;
import org.lucas.arbackend.repository.payment.PaymentLogRepository;
import org.lucas.arbackend.service.cache.CacheService;
import org.lucas.arbackend.util.tenant.TenantProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import javax.management.BadAttributeValueExpException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PayFastSubscriptionService {

    private final OrganisationRepository orgRepo;
    private final PaymentLogRepository logRepo;
    private final OrganisationMapper orgMapper;
    private final TenantProvider tenantProvider;
    private final CacheService cacheService;
    private final SubscriptionPlanRepository subPlanRepo;

    private final ObjectMapper mapper = getObjectMapper();
    private final RestClient restClient = RestClient.builder().baseUrl("https://api.payfast.co.za").build();

    @Value("${payfast.pass-phrase}")
    private String passphrase;

    @Value("${payfast.merchant-id}")
    private String merchantId;

    @Value("${payfast.sandbox-url}")
    private String sandboxUrl;

    public String processIpn(HttpServletRequest request) {
        if (passphrase == null || passphrase.isBlank()) {
            throw new IllegalStateException("No passphrase provided");
        }

        // 1. Extract params
        LinkedHashMap<String, String> params = new LinkedHashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);

            params.put(paramName, paramValue != null ? paramValue : "");
        }

        try {
            // Use the correct PayFast key 'payment_status'
            PaymentStatus paymentStatus = PaymentStatus.valueOf(params.get("payment_status"));
            log.info("DEBUG: Payment status from incoming params [{}]. Payment status from parsing [{}]", params.get("payment_status"), paymentStatus);
            FailureCode failureCode = null;
            String failureDescription = null;
            SubscriptionStatus subscriptionStatus = null;

            // Defensive parsing
            Long orgId = params.containsKey("m_payment_id") ? Long.parseLong(params.get("m_payment_id")) : null;
            String payFastId = params.get("pf_payment_id");

            // 2. Validations
            Organisation org = (orgId != null) ? orgRepo.findById(orgId).orElse(null) : null;
            if (org == null) {
                failureCode = FailureCode.ORG_NOT_FOUND;
                failureDescription = "Org ID " + orgId + " not found.";
            }

            PlanTypes planTerm = null;
            SubscriptionPlan subscriptionPlan = null;
            try {
                planTerm = PlanTypes.valueOf(params.get("item_name").toUpperCase());
                subscriptionPlan = subPlanRepo.findByPlan(planTerm).orElseThrow();
            } catch (Exception e) {
                failureCode = FailureCode.PLAN_MISMATCH;
                failureDescription = "Invalid plan name: " + params.get("item_name");
            }

            double receivedAmount = params.containsKey("amount_gross") ? Double.parseDouble(params.get("amount_gross")) : 1;
            if (subscriptionPlan != null && subscriptionPlan.getPrice() != null) {
                if (!subscriptionPlan.getPrice().equals(receivedAmount)) {
                    failureCode = FailureCode.AMOUNT_MISMATCH;
                    failureDescription = "Price mismatch. Expected price = [" + subscriptionPlan.getPrice() +"]. Received amount = [" + receivedAmount + "]";
                }
            }
            // Signature Check
            String receivedSignature = params.get("signature");
            if (receivedSignature == null || !receivedSignature.equals(calculateItnSignature(params, passphrase))) {
                failureCode = FailureCode.SIGNATURE_MISMATCH;
                failureDescription = "Signature mismatch!";
            }

            // Duplicate Check
            if (logRepo.existsByPfPaymentId(payFastId)) {
                failureCode = FailureCode.DUPLICATE_PAYMENT;
                failureDescription = "Already processed pf_id: " + payFastId;
                // Return "OK" immediately to stop PayFast from retrying a success
                return "OK";
            }

            // 3. EXECUTION BLOCK (Only if no failures)
            if (failureCode == null && "COMPLETE".equalsIgnoreCase(paymentStatus.name())) {
                subscriptionStatus = SubscriptionStatus.ACTIVE;
                updateOrganisationSubscription(org, params.get("item_name"));
                cacheService.updateCache("auth_user", org.getEmail(), orgMapper.mapToOrgResponse(org));
                log.info("Successfully processed payment for Org {}", orgId);
            } else if (failureCode != null) {
                log.error("Payment Flagged: {} - {}", failureCode, failureDescription);
            }

            // 4. ALWAYS SAVE THE LOG
            logRepo.save(PaymentLog.builder()
                    .pfPaymentId(payFastId)
                    .orgId(orgId)
                    .amount(BigDecimal.valueOf(receivedAmount))
                    .planTerm(planTerm)
                    .subscriptionCycles(params.get("cycles") != null ? Integer.parseInt(params.get("cycles")) : 1)
                    .paymentStatus(paymentStatus)
                    .subscriptionStatus(subscriptionStatus)
                    .failureCode(failureCode)
                    .failureDetails(failureDescription)
                    .token(params.get("token"))
                    .billingDate(params.get("billing_date") != null ?
                            LocalDateTime.parse(params.get("billing_date") + "T00:00:00") : LocalDateTime.now())
                    .build());

        } catch (Exception e) {
            log.error("Critical ITN Error", e);
            // Return 500 so PayFast retries
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "ITN Failed");
        }

        return "OK";
    }

    /**
     * Calls the PayFast API to fetch the current status of a recurring subscription.
     */
    public PayFastSubscriptionDto fetchSubscriptionStatus()  {
        PaymentLog paymentLog = logRepo.findByOrgId(tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("No token found for organisation"));

        Map<String, String> headerMap = generateApiHeaders();

        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                .path("/subscriptions/{token}/fetch")
                .queryParam("testing", "true")
                .build(paymentLog.getToken()))
                .accept(MediaType.APPLICATION_JSON)
                .headers(httpHeaders -> headerMap.forEach(httpHeaders::add))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) ->
                        Mono.error(new BadAttributeValueExpException("Error fetching subscription status")))
                .body(String.class);

        log.debug("Response: [{}]", response);

        try {
            return mapper.readValue(response, PayFastSubscriptionDto.class);
        } catch (Exception e) {
            throw  new IllegalStateException("Failed to map PayFast response: ", e);
        }
    }

    public boolean cancelPayFastSubscription() {
        Map<String, String> headerMap = generateApiHeaders();

        PaymentLog paymentLog = logRepo.findByOrgId(tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("No subscription plan found for organisation " + tenantProvider.get()));

        if (paymentLog.getEndedAt() != null) {
            throw new IllegalStateException("Subscription plan already cancelled");
        }

        String response = restClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/subscriptions/{token}/cancel")
                        .queryParam("testing", true)
                        .build(paymentLog.getToken()))
                .headers(httpHeaders -> headerMap.forEach(httpHeaders::add))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) ->
                        Mono.error(new BadAttributeValueExpException("Error cancelling subscription")))
                .body(String.class);

        log.info("Subscription cancellation response: [{}]", response);

        try {
            JsonNode root = mapper.readTree(response);

            int code = root.path("code").asInt();
            String status = root.path("status").asText();

            boolean wasCancelled = root.path("data").path("response").asBoolean();

            if (code == 200 && status.equals("success") && wasCancelled) {
                logRepo.delete(paymentLog);
                return true;
            }

            return false;
        } catch (Exception e) {
            throw new InternalException("Unable to parse response from PayFast subscription cancellation: ", e);
        }
    }

    /**
     * Generates headers using the alphabetical sort required by the API.
     */
    private Map<String, String> generateApiHeaders() {
        String timestamp = ZonedDateTime.now(ZoneId.of("Africa/Johannesburg"))
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));

        Map<String, String> sortedMap = new TreeMap<>();
        sortedMap.put("merchant-id", merchantId);
        sortedMap.put("version", "v1");
        sortedMap.put("timestamp", timestamp);
        if (passphrase != null) {
            sortedMap.put("passphrase", passphrase.trim());
        } else {
            throw new RuntimeException("Pass phrase is null");
        }

    // 3. Construct base string with RAW values
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> treeMap : sortedMap.entrySet()) {
            String key = treeMap.getKey();
            String value = treeMap.getValue();

            sb.append(key).append("=").append(payFastEncode(value)).append("&");
        }

        String baseString = sb.substring(0, sb.length() - 1);

        log.info("RAW Base String for MD5: {}", baseString);

    // 4. Return headers (Passphrase is NOT a header, only used for signature)
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


// TODO: Need to check at month end if the recurring payment was made and update as necessary
/**
 * Updates the subscription for an organization based on the provided subscription term.
 * This method handles both new subscriptions and renewals of existing ones.
 *
 * @param org The organization whose subscription needs to be updated
 * @param subTerm The subscription term (e.g., "MONTHLY" or "YEARLY")
 * @throws RuntimeException If no subscription type is provided
 */
    private void updateOrganisationSubscription(Organisation org, String subTerm) {
    // Validate that subscription term is provided
        if (subTerm.isBlank()) throw new RuntimeException("No subscription type provided");

    // Convert the subscription term to the corresponding PlanTypes enum value
        PlanTypes purchasedPlan = PlanTypes.valueOf(subTerm.toUpperCase());
    // Get the organization's current subscription
        OrganisationSubscription sub = org.getSubscription();
    // Find the subscription plan in the repository based on the purchased plan
        SubscriptionPlan subPlan = subPlanRepo.findByPlan(purchasedPlan)
                .orElseThrow(() -> new EntityNotFoundException("Subscription plan not found"));

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
            log.info("DEBUG: MD5 Signature: [{}]", sb);
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private ObjectMapper getObjectMapper () {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.registerModule(new JavaTimeModule());
    }

}