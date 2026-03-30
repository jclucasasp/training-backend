package org.lucas.arbackend.service.payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.dto.payfast.PayFastSubscriptionDto;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.OrganisationSubscription;
import org.lucas.arbackend.entity.Organisation.SubscriptionPlan;
import org.lucas.arbackend.entity.PlanTypes;
import org.lucas.arbackend.entity.payment.PaymentLog;
import org.lucas.arbackend.mapper.OrganisationMapper;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.SubscriptionPlanRepository;
import org.lucas.arbackend.repository.payment.PaymentLogRepository;
import org.lucas.arbackend.service.cache.CacheService;
import org.lucas.arbackend.util.tenant.TenantProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
@RequiredArgsConstructor
public class PayFastSubscriptionService {

    private final OrganisationRepository orgRepo;
    private final PaymentLogRepository logRepo;
    private final OrganisationMapper orgMapper;
    private final TenantProvider tenantProvider;
    private final CacheService cacheService;
    private final SubscriptionPlanRepository subPlanRepo;
    private final RestClient restClient = RestClient.builder().baseUrl("https://api.payfast.co.za").build();

    @Value("${payfast.pass-phrase}")
    private String passphrase;

    @Value("${payfast.merchant-id}")
    private String merchantId;

    @Value("${payfast.sandbox-url}")
    private String sandboxUrl;

    /**
 * Processes an IPN (Instant Payment Notification) from PayFast, verifying the signature
 * and updating the organization's subscription and payment records accordingly.
 *
 * @param request The HttpServletRequest containing the PayFast notification parameters
 * @throws RuntimeException If no passphrase is provided for signature validation
 * @throws SecurityException If the PayFast signature is invalid
 * @throws EntityNotFoundException If the organization is not found
 */
    // TODO: Verify that the amount send from payfast corresponds to the amount stored in the db
    @Transactional
    public void processIpn(HttpServletRequest request) {
    // Check if passphrase is provided for signature validation
        if (passphrase == null || passphrase.isBlank()) {
            throw new RuntimeException("No passphrase provided for PayFast signature validation");
        }

    // Extract all parameters from the request into a LinkedHashMap
        Map<String, String> params = new LinkedHashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            params.put(paramName, paramValue != null ? paramValue : "");
        }

    // Log the received notification for organization tracking
        log.info("Received PayFast ITN notification for Organisation ID: {}", params.get("m_payment_id"));

        // FIX: Compare the received signature with the calculated one
        String receivedSignature = params.get("signature");
        if (receivedSignature == null || !receivedSignature.equals(calculateItnSignature(params, passphrase))) {
            throw new SecurityException("Invalid PayFast signature detected");
        }

        // TODO: Throw a custom exception for invalid signatures
        if (!"COMPLETE".equalsIgnoreCase(params.get("payment_status"))) {
            log.warn("Ignored IPN: Status is {}", params.get("payment_status"));
            return;
        }

        String pfId = params.get("pf_payment_id");
        if (logRepo.existsByPfPaymentId(pfId)) {
            log.warn("Payment {} already processed.", pfId);
            return;
        }

        Long orgId = Long.parseLong(params.get("m_payment_id"));
        double gross = Double.parseDouble(params.get("amount_gross"));

        Organisation org = orgRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Org not found"));

        updateSubscription(org, params.get("item_name"));

        logRepo.save(PaymentLog.builder()
                .pfPaymentId(pfId)
                .orgId(orgId)
                .amount(BigDecimal.valueOf(gross))
                .subscription(params.get("token") != null)
                .token(params.get("token"))
                .billingDate(LocalDateTime.parse(params.get("billing_date") + "T00:00:00"))
                .paymentStatus("SUCCESS")
                .build());

        cacheService.updateCache("auth_user", org.getEmail(), orgMapper.mapToOrgResponse(org));
    }

    /**
     * Calls the PayFast API to fetch the current status of a recurring subscription.
     */
    public PayFastSubscriptionDto fetchSubscriptionStatus()  {
        PaymentLog paymentLog = logRepo.findByOrgId(tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("No token found for organisation"));

        Map<String, String> treeMap = generateApiHeaders();

        String response = restClient.get()
                .uri(uriBuilder -> uriBuilder
                .path("/subscriptions/{token}/fetch")
                .queryParam("testing", "true")
                .build(paymentLog.getToken()))
                .accept(MediaType.APPLICATION_JSON)
                .header("merchant-id", treeMap.get("merchant-id"))
                .header("version", "v1")
                .header("timestamp", treeMap.get("timestamp"))
                .header("signature", treeMap.get("signature"))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) ->
                        Mono.error(new RuntimeException("Error fetching subscription status")))
                .body(String.class);

        log.debug("Response: [{}]", response);

        return mapToPayFastSubscriptionDTO(response);

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
            log.info("DEBUG: MD5 Signature: [{}]", sb.toString());
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

    private PayFastSubscriptionDto mapToPayFastSubscriptionDTO (String response) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());

        try {
            JsonNode root = mapper.readTree(response);
            JsonNode dataNode = root.path("data").path("response");

            return mapper.treeToValue(dataNode, PayFastSubscriptionDto.class);
        } catch (Exception e) {
            throw  new RuntimeException("Failed to map PayFast response: ", e);
        }
    }

}