package org.lucas.arbackend.service.security;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.security.ApiKeyResponse;
import org.lucas.arbackend.entity.Organisation.OrganisationSubscription;
import org.lucas.arbackend.entity.security.ApiKey;
import org.lucas.arbackend.repository.organisation.OrganisationSubscriptionRepository;
import org.lucas.arbackend.repository.security.ApiKeyRepository;
import org.lucas.arbackend.service.cache.CacheService;
import org.lucas.arbackend.util.TenantProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor @Transactional
public class ApiKeyService {

    private final PasswordEncoder passwordEncoder;
    private final ApiKeyRepository apiKeyRepo;
    private final CacheService cacheService;
    private final TenantProvider tenantProvider;
    private final OrganisationSubscriptionRepository subscriptionRepo;

    public ApiKeyResponse generateKeyForOrg(ApiKey apiKey, boolean isNewSignup) {

        if (!isNewSignup && apiKey.getOrgId() != null) {
            // Use Redis cache for lookups
            OrganisationSubscription sub = subscriptionRepo.findActiveByOrganisationId(apiKey.getOrgId())
                    .orElseThrow(() -> new EntityNotFoundException("No active subscription found for organisation"));

            if (apiKeyRepo.existsById(apiKey.getOrgId())) {
                throw new IllegalStateException("API Key already exists for organisation");
            }
        }

        // Generate API Key by concatenating two UUIDs without hyphens
        String rawKey = "sk_" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");

        // 2. Hash it for storage
        String hashedKey = passwordEncoder.encode(rawKey);

        // 3. Save metadata + hash
        apiKey.setPrefix(rawKey.substring(0, 12));
        apiKey.setHashKey(hashedKey); // We never store the raw key

        // 4. Return the RAW key to the user
        return ApiKeyResponse.builder()
                .rawKey(rawKey) // Critical: Frontend must display this immediately
                .prefix(rawKey.substring(0, 12) + "...") // For UI listing later
                .createdAt(LocalDateTime.now())
                .build();
    }


    public void revokeApiKeyForOrg() {

        Long orgId = tenantProvider.get();
        // Find the API key by its ID, throw exception if not found
        ApiKey apiKey = apiKeyRepo.findByOrganisation_Id(orgId).orElseThrow(() -> new EntityNotFoundException("API Key not found"));

        // Check if the current organization matches the API key's organization
        if (!apiKey.getOrgId().equals(orgId)) {
            throw new AccessDeniedException("Cannot revoke API Key for another organisation");
        }

        // Remove the API key from cache and delete it from the repository
        cacheService.evictApiKey(apiKey.getPrefix());
        apiKeyRepo.delete(apiKey);
    }
}
