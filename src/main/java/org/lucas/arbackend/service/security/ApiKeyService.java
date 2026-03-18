package org.lucas.arbackend.service.security;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.CacheDto;
import org.lucas.arbackend.dto.security.ApiKeyResponse;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.OrganisationSubscription;
import org.lucas.arbackend.entity.security.ApiKey;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.security.ApiKeyRepository;
import org.lucas.arbackend.service.cache.CacheService;
import org.lucas.arbackend.util.TenantProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ApiKeyService {

    private final PasswordEncoder passwordEncoder;
    private final ApiKeyRepository apiKeyRepo;
    private final CacheService cacheService;
    private final TenantProvider tenantProvider;
    private final OrganisationRepository orgRepo;

    public ApiKeyResponse generateKeyForOrg(ApiKey apiKey, boolean isNewSignup) {
        Organisation org = null;

        if (!isNewSignup) {
            org = orgRepo.findById(tenantProvider.get()).orElseThrow(() -> new EntityNotFoundException("Organisation not found"));
            if (org.getSubscription().getStatus().equals(0)) {
                throw new IllegalStateException("Organisation has no active subscription");
            }

            if (apiKeyRepo.existsById(org.getId()) && org.getApiKey() != null) {
                throw new IllegalStateException("API Key already exists for organisation. If you need a new one, then revoke the old one first.");
            }
            apiKey.setOrganisation(org);
            org.setApiKey(apiKey);
        }

        // Generate API Key by concatenating two UUIDs without hyphens
        String rawKey = "sk_" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");

        // 2. Hash it for storage
        String hashedKey = passwordEncoder.encode(rawKey);

        // 3. Save metadata + hash
        apiKey.setPrefix(rawKey.substring(0, 12));
        apiKey.setHashKey(hashedKey); // We never store the raw key

        if (!isNewSignup) {
            orgRepo.save(apiKey.getOrganisation());
            boolean isActiveSubscription = Optional.ofNullable(org.getSubscription()).map(OrganisationSubscription::getStatus).orElse(0) == 1;
            String apiKeyPrefix = Optional.ofNullable(org.getApiKey()).map(ApiKey::getPrefix).orElse("");
            cacheService.updateCache("auth_user", org.getEmail(), new CacheDto(org.getId(), org.getEmail(), org.getPassword(), org.getFirstName(), org.getLastName(), org.getContactNumber(), org.getRole().getName(), org.getId(), apiKeyPrefix, isActiveSubscription));
        }

        // 4. Return the RAW key to the user
        return ApiKeyResponse.builder()
                .rawKey(rawKey) // Critical: Frontend must display this immediately
                .prefix(rawKey.substring(0, 12) + "...") // For UI listing later
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void revokeApiKeyForOrg() {

        Organisation org = orgRepo.findById(tenantProvider.get()).orElseThrow(() -> new EntityNotFoundException("Organisation not found"));
        // Find the API key by its ID, throw exception if not found
        ApiKey apiKey = apiKeyRepo.findByOrganisation_Id(org.getId()).orElseThrow(() -> new EntityNotFoundException("API Key not found"));

        // Check if the current organization matches the API key's organization
        if (!apiKey.getOrgId().equals(org.getId())) {
            throw new AccessDeniedException("Cannot revoke API Key for another organisation");
        }

        // Remove the API key from cache and delete it from the repository
        cacheService.evictApiKey(apiKey.getPrefix());
        org.setApiKey(null);
        orgRepo.save(org);

        boolean isActiveSubscription = Optional.ofNullable(org.getSubscription()).map(OrganisationSubscription::getStatus).orElse(0) == 1;
        String apiKeyPrefix = Optional.ofNullable(org.getApiKey()).map(ApiKey::getPrefix).orElse("");
        cacheService.updateCache("auth_user", org.getEmail(), new CacheDto(org.getId(), org.getEmail(), org.getPassword(), org.getFirstName(), org.getLastName(), org.getContactNumber(), org.getRole().getName(), org.getId(), apiKeyPrefix, isActiveSubscription));
    }
}
