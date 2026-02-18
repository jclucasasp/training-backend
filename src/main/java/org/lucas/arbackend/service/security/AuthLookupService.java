package org.lucas.arbackend.service.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.lucas.arbackend.dto.CacheDto;
import org.lucas.arbackend.dto.security.ApiKeyResponse;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.StaffRepository;
import org.lucas.arbackend.repository.security.ApiKeyRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class AuthLookupService {

    private final OrganisationRepository orgRepo;
    private final StaffRepository staffRepo;
    private final ApiKeyRepository apiKeyRepo;

    @Cacheable(value = "auth_user", key = "#email", unless = "#result == null")
    public CacheDto getAuthCacheDto(String email) {

        return orgRepo.findByEmail(email)
                .map(org ->
                        new CacheDto(
                                org.getId(),
                                org.getEmail(),
                                org.getPassword(),
                                org.getFirstName(),
                                org.getLastName(),
                                org.getContactNumber(),
                                org.getRole().getName(),
                                org.getId()
                        ))
                .orElseGet(() -> staffRepo.findByEmail(email)
                        .map(staff -> new CacheDto
                                (
                                        staff.getId(),
                                        staff.getEmail(),
                                        staff.getPassword(),
                                        staff.getFirstName(),
                                        staff.getLastName(),
                                        staff.getContactNumber(),
                                        staff.getRole().getName(),
                                        staff.getOrganisation().getId()
                                )).orElseThrow(() -> new UsernameNotFoundException("User not found: " + email)));

    }

    @Cacheable(value = "api_key", key = "#prefix", unless = "#result == null")
    public ApiKeyResponse getApiKey(String prefix) throws BadRequestException {
        return apiKeyRepo.findByPrefix(prefix)
                .map(key -> new ApiKeyResponse(key.getOrgId(), null, key.getPrefix(), key.getHashKey(), key.getCreatedAt()))
                .orElseThrow(() -> new BadRequestException("API Key not found: " + prefix));
    }
}
