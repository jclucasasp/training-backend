package org.lucas.arbackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.lucas.arbackend.dto.organisation.ApiKeyResponse;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

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


    // TODO: Check flow and fix bugs
    // ==========================================
    // 1. ATOMIC SIGN UP (Org + Profile + Sub)
    // ==========================================
    public OrganisationResponse signUp(OrgSignupRequest request) {
        // 1. Validation
        if (orgRepo.findByOrgEmail(request.getEmail()).isPresent()) {
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
        profileRepo.save(profile);

        // 4. Assign Initial Subscription (Default to ID 1 or specific plan)
        Long planId = request.getInitialPlanId() != null ? request.getInitialPlanId() : 1L;
        SubscriptionPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found"));

        OrganisationSubscription sub = new OrganisationSubscription();
        sub.setId(savedOrg.getId());
        sub.setPlanId(plan.getId());
        sub.setOsStartDate(LocalDateTime.now());
        // Simple logic: monthly sub
        sub.setOsEndDate(LocalDateTime.now().plusMonths(1));
        sub.setOsStatus(true); // Active
        subRepo.save(sub);

        return OrganisationResponse.builder()
                .id(savedOrg.getId())
                .email(savedOrg.getEmail())
                .orgName(profile.getOrgName())
                .subscriptionStatus("ACTIVE")
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
    public OrganisationResponse getOrganisationDetails(Long orgId) {
        Organisation org = orgRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        Profile profile = profileRepo.findById(orgId).orElse(new Profile()); // Fallback

        // Check for active sub
        boolean isActive = subRepo.findActiveSubscription(orgId).isPresent();

        return OrganisationResponse.builder()
                .id(org.getId())
                .email(org.getEmail())
                .orgName(profile.getOrgName())
                .subscriptionStatus(isActive ? "ACTIVE" : "INACTIVE")
                .build();
    }

    // ==========================================
    // 3. SECURE API KEY GENERATION
    // ==========================================
    public ApiKeyResponse generateApiKey(Long orgId) {
        // 1. Generate a raw random key (User sees this ONLY once)
        String rawKey = "sk_" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");

        // 2. Hash it for storage
        String hashedKey = passwordEncoder.encode(rawKey);

        // 3. Save metadata + hash
        ApiKey apiKey = new ApiKey();
        apiKey.setId(orgId);
        apiKey.setValue(hashedKey); // We never store the raw key

        apiKeyRepo.save(apiKey);

        // 4. Return the RAW key to the user
        return ApiKeyResponse.builder()
                .rawKey(rawKey) // Critical: Frontend must display this immediately
                .prefix(rawKey.substring(0, 8) + "...") // For UI listing later
                .createdAt(LocalDateTime.now())
                .build();
    }

    public void revokeApiKey(Long orgId, Long keyId) {
        ApiKey key = apiKeyRepo.findById(keyId)
                .orElseThrow(() -> new EntityNotFoundException("Key not found"));

        if (!key.getId().equals(orgId)) {
            throw new SecurityException("Unauthorized access to API key");
        }

        key.setEndedAt(LocalDateTime.now()); // Soft delete
        apiKeyRepo.save(key);
    }

}