package org.lucas.arbackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.relationship.OrgApiRel;
import org.lucas.arbackend.entity.security.ApiKey;
import org.lucas.arbackend.repository.OrganisationRepository;
import org.lucas.arbackend.repository.relationship.OrgApiRelRepository;
import org.lucas.arbackend.repository.security.ApiKeyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepo;


    @Transactional
    public String generateKeyForOrg(Long orgId) {
        Organisation org = orgRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        // Generate unique key
        ApiKey key = ApiKey.builder()
                .hashKey(UUID.randomUUID().toString())
                .build();
        ApiKey savedKey = apiKeyRepo.save(key);

        // Map to Organisation
        OrgApiRel rel = OrgApiRel.builder()
                .organisation(org)
                .apiKey(savedKey)
                .build();
        relRepo.save(rel);

        return savedKey.getHashKey();
    }
}
