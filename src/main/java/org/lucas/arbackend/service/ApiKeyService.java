package org.lucas.arbackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.security.ApiKeyResponse;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.Profile;
import org.lucas.arbackend.entity.security.ApiKey;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.ProfileRepository;
import org.lucas.arbackend.repository.security.ApiKeyRepository;
import org.lucas.arbackend.util.TenantContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepo;
    private final ProfileRepository profileRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ApiKeyResponse generateKeyForOrg(Long orgId) {

//        Long orgId = TenantContext.getCurrentTenant();

        // Check if the Organisation exists
        Profile profile = profileRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation Profile not found"));

        // Generate API Key
        String rawKey = "sk_" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");

        // 2. Hash it for storage
        String hashedKey = passwordEncoder.encode(rawKey);

        // 3. Save metadata + hash
        ApiKey apiKey = new ApiKey();
        apiKey.setProfile(profile);
        apiKey.setPrefix(rawKey.substring(0, 12));
        apiKey.setHashKey(hashedKey); // We never store the raw key

        apiKeyRepo.save(apiKey);

        // 4. Return the RAW key to the user
        return ApiKeyResponse.builder()
                .rawKey(rawKey) // Critical: Frontend must display this immediately
                .prefix(rawKey.substring(0, 12) + "...") // For UI listing later
                .createdAt(LocalDateTime.now())
                .build();
    }
}
