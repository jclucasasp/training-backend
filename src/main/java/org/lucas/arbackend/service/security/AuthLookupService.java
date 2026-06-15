package org.lucas.arbackend.service.security;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.lucas.arbackend.dto.CacheDto;
import org.lucas.arbackend.dto.security.ApiKeyResponse;
import org.lucas.arbackend.entity.Organisation.OrganisationSubscription;
import org.lucas.arbackend.entity.security.ApiKey;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.OrganisationSubscriptionRepository;
import org.lucas.arbackend.repository.organisation.StaffRepository;
import org.lucas.arbackend.repository.security.ApiKeyRepository;
import org.lucas.arbackend.repository.student.StudentRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public class AuthLookupService {

    private final OrganisationRepository orgRepo;
    private final StaffRepository staffRepo;
    private final StudentRepository studentRepo;
    private final ApiKeyRepository apiKeyRepo;
    private final OrganisationSubscriptionRepository subRepo;


/**
 * This method retrieves user authentication data from cache or database based on email.
 * It first checks the organization repository, then the staff repository if not found.
 * The result is cached for future requests.
 *
 * @param email The email of the user to retrieve
 * @return CacheDto containing user authentication data
 * @throws UsernameNotFoundException if user is not found in either repository
 */

    @Cacheable(value = "auth_user", key = "#email", unless = "#result == null")
    public CacheDto getAuthCacheDto(String email) {

        // First, try to find the user in the organization repository
        return orgRepo.findByEmail(email)
                .map(org -> {
                    boolean isSubscriptionActive = Optional.ofNullable(org.getSubscription()).map(OrganisationSubscription::getStatus).orElse(0)== 1;
                    String apiKey = Optional.ofNullable(org.getApiKey()).map(ApiKey::getPrefix).orElse("");
                    // Create CacheDto from organization data if found
                    return new CacheDto(
                            org.getId(),
                            org.getEmail(),
                            org.getPassword(),
                            org.getFirstName(),
                            org.getLastName(),
                            org.getContactNumber(),
                            org.getRole().getRoleName().name(),
                            org.getId(),
                            apiKey,
                            isSubscriptionActive
                    );
                })
                // If not found in organization, try staff repository
                .orElseGet(() -> staffRepo.findByEmail(email)
                        .map(staff -> {
                            boolean isSubscriptionActive = Optional.ofNullable(staff.getOrganisation().getSubscription()).map(OrganisationSubscription::getStatus).orElse(0) == 1;
                            String apiKey = Optional.ofNullable(staff.getOrganisation().getApiKey()).map(ApiKey::getPrefix).orElse("");
                            // Create CacheDto from staff data
                            return new CacheDto
                                    (
                                            staff.getId(),
                                            staff.getEmail(),
                                            staff.getPassword(),
                                            staff.getFirstName(),
                                            staff.getLastName(),
                                            staff.getContactNumber(),
                                            staff.getRole().getRoleName().name(),
                                            staff.getOrganisation().getId(),
                                            apiKey,
                                            isSubscriptionActive
                                    );
                        })
                        .orElseGet(() -> studentRepo.findByEmail(email)
                                .map(student -> {
                                    boolean isSubscriptionActive = Optional.ofNullable(student.getOrganisation().getSubscription())
                                            .map(OrganisationSubscription::getStatus).orElse(0) == 1;
                                    String apiKey = Optional.ofNullable(student.getOrganisation().getApiKey())
                                            .map(ApiKey::getPrefix).orElse("");
                                    // Create CacheDto from staff data
                                    return new CacheDto
                                            (
                                                    student.getId(),
                                                    student.getEmail(),
                                                    student.getPassword(),
                                                    student.getFirstName(),
                                                    student.getLastName(),
                                                    "",
                                                    student.getRole().getRoleName().name(),
                                                    student.getOrganisation().getId(),
                                                    apiKey,
                                                    isSubscriptionActive
                                            );
                                })
                                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email))));

    }

    /**
     * Retrieves an API key based on the provided prefix.
     * This method uses Spring's caching abstraction to cache the results.
     * The cache is named "api_key" and uses the prefix as the cache key.
     * The result will not be cached if it's null.
     *
     * @param prefix The prefix of the API key to retrieve
     * @return ApiKeyResponse containing the API key information and subscription status
     * @throws BadRequestException if there's an error with the request
     */
    @Cacheable(value = "api_key", key = "#prefix", unless = "#result == null")
    public ApiKeyResponse getApiKey(String prefix) throws BadRequestException {

        // Find API key by prefix or throw exception if not found
        ApiKey apiKey = apiKeyRepo.findByPrefix(prefix).orElseThrow(() -> new EntityNotFoundException("API Key not found: " + prefix));
        log.info("DEBUG: Api key found for organisation [{}]", apiKey.getOrgId());
        // Check if the subscription is active for the organization
        boolean isSubscriptionActive = subRepo.findActiveByOrganisationId(apiKey.getOrgId())
                .orElseThrow(() -> new EntityNotFoundException("Subscription not found")).getStatus() == 1;

        // Return API key response with organization ID, null for user ID, prefix, hash key, creation timestamp, and subscription status
        return new ApiKeyResponse(apiKey.getOrgId(), null, apiKey.getPrefix(), apiKey.getHashKey(), apiKey.getCreatedAt(), isSubscriptionActive);
    }
}
