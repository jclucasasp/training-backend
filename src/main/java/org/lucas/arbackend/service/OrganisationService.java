package org.lucas.arbackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.lucas.arbackend.dto.security.ApiKeyResponse;
import org.lucas.arbackend.dto.organisation.OrgSignupRequest;
import org.lucas.arbackend.dto.organisation.OrganisationResponse;
import org.lucas.arbackend.dto.organisation.ProfileRequest;
import org.lucas.arbackend.entity.BaseEntity;
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
import java.util.Optional;
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
        profile.setOrgName(request.getOrgName());
        profile.setRegistrationNumber(request.getRegistrationNumber());
        profile.setVatNumber(request.getVatNumber());
        profileRepo.save(profile);

        // Generate API Key
        String rawKey = "sk_" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");

        // 2. Hash it for storage
        String hashedKey = passwordEncoder.encode(rawKey);

        // 3. Save metadata + hash
        ApiKey apiKey = new ApiKey();
        apiKey.setOrganisation(org);
        apiKey.setHashKey(hashedKey); // We never store the raw key

        apiKeyRepo.save(apiKey);

        // 4. Assign Initial Subscription (Default to ID 1 or specific plan)
        // TODO: Change this after testing
        Long planId = request.getInitialPlanId() != null ? request.getInitialPlanId() : 1L;
        SubscriptionPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found"));

        OrganisationSubscription sub = new OrganisationSubscription();
        sub.setId(savedOrg.getId());
        sub.getSubscriptionPlan().setId(plan.getId());
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
                .apiKey(rawKey)
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
    public OrganisationResponse getOrganisationDetails(Long orgId) {
        Organisation org = orgRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        Profile profile = profileRepo.findById(orgId).orElse(new Profile()); // Fallback

        // Check for active sub
        Optional<OrganisationSubscription> activeSubscription = subRepo.findActiveSubscription(orgId);
        boolean hasActiveSubscription = activeSubscription.isPresent();
        String plan = hasActiveSubscription ? activeSubscription.get().getSubscriptionPlan().getPlan().toString() : null;

        return OrganisationResponse.builder()
                .id(org.getId())
                .email(org.getEmail())
                .orgName(profile.getOrgName())
                .registrationNumber(profile.getRegistrationNumber())
                .vatNumber(profile.getVatNumber())
                .orgSignedUpDate(org.getCreatedAt())
                .orgLastUpdated(org.getUpdatedAt())
                .orgDeletedDate(org.getEndedAt())
                .subscriptionStatus(hasActiveSubscription)
                .subscriptionPlan(plan)
                .subscriptionStartDate(activeSubscription.map(BaseEntity::getCreatedAt).orElse(null))
                .subscriptionEndDate(activeSubscription.map(BaseEntity::getEndedAt).orElse(null))
                .build();
    }

    // ==========================================
    // 3. SECURE API KEY GENERATION
    // ==========================================
    public ApiKeyResponse generateApiKey(Long orgId) {
        // Make sure the Organisation exist
        Organisation org = orgRepo.findById(orgId).orElseThrow(()-> new EntityNotFoundException("Organisation not found"));

        // 1. Generate a raw random key (User sees this ONLY once)
        String rawKey = "sk_" + UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");

        // 2. Hash it for storage
        String hashedKey = passwordEncoder.encode(rawKey);

        // 3. Save metadata + hash
        ApiKey apiKey = new ApiKey();
        apiKey.setOrganisation(org); // Automatically assign the Organisation id
        apiKey.setHashKey(hashedKey); // We never store the raw key

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

        if (!key.getOrgId().equals(orgId)) {
            throw new SecurityException("Unauthorized access to API key");
        }

        key.setEndedAt(LocalDateTime.now()); // Soft delete
        apiKeyRepo.save(key);
    }

}