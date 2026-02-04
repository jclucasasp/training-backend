package org.lucas.arbackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.dto.security.ApiKeyResponse;
import org.lucas.arbackend.dto.organisation.OrgSignupRequest;
import org.lucas.arbackend.dto.organisation.OrganisationResponse;
import org.lucas.arbackend.dto.organisation.ProfileRequest;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.OrganisationSubscription;
import org.lucas.arbackend.entity.Organisation.Profile;
import org.lucas.arbackend.entity.SubscriptionPlan;
import org.lucas.arbackend.entity.security.ApiKey;
import org.lucas.arbackend.repository.SubscriptionPlanRepository;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.OrganisationSubscriptionRepository;
import org.lucas.arbackend.repository.organisation.ProfileRepository;
import org.lucas.arbackend.repository.security.ApiKeyRepository;
import org.lucas.arbackend.util.TenantContext;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class OrganisationService {

    private final OrganisationRepository orgRepo;
    private final ProfileRepository profileRepo;
    private final SubscriptionPlanRepository planRepo;
    private final OrganisationSubscriptionRepository subRepo;
    private final ApiKeyRepository apiKeyRepo;
    private final PasswordEncoder passwordEncoder;
    private final CacheService cacheService;

    private final ApiKeyService apiKeyService;

    // TODO: Implement a check to make sure a company can not generate more then one API key. Implement a new method to be able to end the old one and generate a new one.
    // ==========================================
    // 1. ATOMIC SIGN UP (Org + Profile + Sub)
    // ==========================================
    public OrganisationResponse signUp(OrgSignupRequest request) {
        // 1. Validation
        if (orgRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }

        // 2. Create Core Organisation
        Organisation org = new Organisation();
        org.setEmail(request.getEmail());
        org.setPassword(passwordEncoder.encode(request.getPassword()));
        // BaseEntity fields like createdAt are handled automatically or by default values
        Organisation savedOrg = orgRepo.save(org);

        // 3. Create Linked Profile (Shares PK)
        Profile profile = new Profile();
        profile.setOrganisation(savedOrg); // Sets ID automatically via MapsId
        profile.setOrgName(request.getOrgName());
        profile.setRegistrationNumber(request.getRegistrationNumber());
        profile.setVatNumber(request.getVatNumber());
        profileRepo.save(profile);

        // Generate API Key
        ApiKeyResponse apiKeyResponse = apiKeyService.generateKeyForOrg(savedOrg.getId());

        if (apiKeyResponse.getRawKey().isBlank()) {
            throw new IllegalStateException("API Key could not be generated");
        }

        // 4. Assign Initial Subscription (Default to ID 1 or specific plan)
        // TODO: Change this after testing
        Long planId = request.getInitialPlanId() != null ? request.getInitialPlanId() : 1L;
        SubscriptionPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found"));

        OrganisationSubscription sub = new OrganisationSubscription();
        sub.setOrganisation(org);
        sub.setSubscriptionPlan(plan);
        // Simple logic: monthly sub
        sub.setEndedAt(LocalDateTime.now().plusMonths(1));
        sub.setStatus(1); // Active
        subRepo.save(sub);

        return OrganisationResponse.builder()
                .id(org.getId())
                .email(org.getEmail())
                .orgName(profile.getOrgName())
                .registrationNumber(profile.getRegistrationNumber())
                .vatNumber(profile.getVatNumber())
                .apiKey(apiKeyResponse.getRawKey())
                .orgSignedUpDate(org.getCreatedAt())
                .orgLastUpdated(org.getUpdatedAt())
                .orgDeletedDate(org.getEndedAt())
                .subscriptionStatus(true)
                .subscriptionPlan(plan.getPlan().toString())
                .subscriptionStartDate(sub.getCreatedAt())
                .subscriptionEndDate(sub.getEndedAt())
                .build();

    }

    // ==========================================
    // 2. PROFILE MANAGEMENT
    // ==========================================
    public void updateProfile(Long orgId, ProfileRequest req) {
        Profile profile = profileRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Profile not found"));

        if (req.getOrgName() != null) profile.setOrgName(req.getOrgName());
        if (req.getRegNumber() != null) profile.setRegistrationNumber(req.getRegNumber());
        if (req.getVatNumber() != null) profile.setVatNumber(req.getVatNumber());

        // No need to call save() if @Transactional is active,
        // Hibernate dirty checking handles it, but explicit save is fine too.
        profileRepo.save(profile);
    }

    @Transactional(readOnly = true)
    public OrganisationResponse getOrganisationDetails() {

        Long orgId = TenantContext.getCurrentTenant();

        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No organisation found for id: [" + orgId + "]");
        }

        Organisation org = orgRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        Profile profile = profileRepo.findById(orgId).orElse(new Profile()); // Fallback

        // Check for active sub
        OrganisationSubscription sub = subRepo.findActiveByOrganisationId(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation subscription not found"));

        String plan = sub.getSubscriptionPlan().getPlan().toString();

        return OrganisationResponse.builder()
                .id(org.getId())
                .email(org.getEmail())
                .orgName(profile.getOrgName())
                .registrationNumber(profile.getRegistrationNumber())
                .vatNumber(profile.getVatNumber())
                .orgSignedUpDate(org.getCreatedAt())
                .orgLastUpdated(org.getUpdatedAt())
                .orgDeletedDate(org.getEndedAt())
                .subscriptionStatus(sub.getStatus() == 1)
                .subscriptionPlan(plan)
                .subscriptionStartDate(sub.getCreatedAt())
                .subscriptionEndDate(sub.getEndedAt())
                .build();
    }
    @Transactional
    public void revokeApiKey(Long orgId, Long keyId) {
        ApiKey key = apiKeyRepo.findById(keyId)
                .orElseThrow(() -> new EntityNotFoundException("Key not found"));

        if (!key.getOrgId().equals(orgId)) {
            throw new SecurityException("Unauthorized access to API key");
        }

        key.setEndedAt(LocalDateTime.now()); // Soft delete
        apiKeyRepo.save(key);

        cacheService.evictApiKey(key.getPrefix());
    }

}