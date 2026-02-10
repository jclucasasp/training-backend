package org.lucas.arbackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.dto.security.ApiKeyResponse;
import org.lucas.arbackend.dto.organisation.OrgSignupRequest;
import org.lucas.arbackend.dto.organisation.OrganisationResponse;
import org.lucas.arbackend.dto.organisation.ProfileRequest;
import org.lucas.arbackend.entity.Organisation.OrgAddress;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.OrganisationSubscription;
import org.lucas.arbackend.entity.Organisation.Profile;
import org.lucas.arbackend.entity.PlanTypes;
import org.lucas.arbackend.entity.SubscriptionPlan;
import org.lucas.arbackend.entity.security.ApiKey;
import org.lucas.arbackend.repository.OrgAddressRepository;
import org.lucas.arbackend.repository.SubscriptionPlanRepository;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.OrganisationSubscriptionRepository;
import org.lucas.arbackend.repository.organisation.ProfileRepository;
import org.lucas.arbackend.repository.security.ApiKeyRepository;
import org.lucas.arbackend.util.TenantContext;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@AllArgsConstructor
@Transactional
public class OrganisationService {

    private final OrganisationRepository orgRepo;
    private final ProfileRepository profileRepo;
    private final OrgAddressRepository addressRepo;
    private final SubscriptionPlanRepository planRepo;
    private final OrganisationSubscriptionRepository subRepo;
    private final ApiKeyRepository apiKeyRepo;
    private final PasswordEncoder passwordEncoder;
    private final CacheService cacheService;

    private final ApiKeyService apiKeyService;

    // TODO: Add soft delete (look at the CourseService for an example)
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
        profile.setContactPerson(request.getContactPerson());
        profile.setContactNumber(request.getContactNumber());

        OrgAddress address = new OrgAddress();
        address.setStreet(request.getStreet());
        address.setSuburb(request.getSuburb());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZip(request.getZip());

        profileRepo.save(profile);

        address.setProfile(profile);
        addressRepo.save(address);

        // Generate API Key
        ApiKeyResponse apiKeyResponse = apiKeyService.generateKeyForOrg(savedOrg.getId());

        if (apiKeyResponse.getRawKey().isBlank()) {
            throw new RuntimeException("API Key could not be generated");
        }

        // 4. Assign Initial Subscription (Default to ID 1 or specific plan)
        // TODO: Change this after testing
        Long planId = request.getInitialPlanId() != null ? request.getInitialPlanId() : 1L;
        SubscriptionPlan plan = planRepo.findById(planId)
                .orElseThrow(() -> new EntityNotFoundException("Plan not found"));

        OrganisationSubscription sub = new OrganisationSubscription();
        sub.setOrganisation(org);
        sub.setSubscriptionPlan(plan);

        // TODO: Change to a switch if more plans get added
        sub.setEndedAt(plan.getPlan().toString().equals(PlanTypes.MONTHLY.name()) ?
                LocalDateTime.now().plusMonths(1) : LocalDateTime.now().plusMonths(12));

        sub.setStatus(1); // Active
        subRepo.save(sub);

        return OrganisationResponse.builder()
                .id(org.getId())
                .email(org.getEmail())
                .orgName(profile.getOrgName())
                .registrationNumber(profile.getRegistrationNumber())
                .vatNumber(profile.getVatNumber())
                .contactPerson(profile.getContactPerson())
                .contactNumber(profile.getContactNumber())
                .streetAddress(address.getStreet())
                .suburb(address.getSuburb())
                .city(address.getCity())
                .zip(address.getZip())
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
    // TODO: Implement a function to update the OrganisationSubscription entity
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

    // This tells the database that it will just be a lookup which speed things up by not doing dirty checking or object snapshots, flushing
    @Transactional(readOnly = true)
    public OrganisationResponse getOrganisationDetails() {

        Long orgId = TenantContext.getCurrentTenant();

        if (orgId == null) {
            throw new IllegalStateException("No Organisation id found");
        }

        Organisation org = orgRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        Profile profile = org.getProfile();

        OrgAddress address = profile != null ? org.getProfile().getAddress() : null;

        OrganisationSubscription subscription = profile != null ? org.getSubscription() : null;

    if (subscription == null) {
        throw new EntityNotFoundException("Organisation subscription not found");
    }

    return OrganisationResponse.builder()
            .id(org.getId())
            .email(org.getEmail())
            .orgName(profile.getOrgName())
            .registrationNumber(profile.getRegistrationNumber())
            .vatNumber(profile.getVatNumber())
            .contactPerson(profile.getContactPerson())
            .contactNumber(profile.getContactNumber())
            .streetAddress(address.getStreet())
            .suburb(address.getSuburb())
            .city(address.getCity())
            .zip(address.getZip())
            .apiKey(profile.getApiKey().getHashKey())
            .orgSignedUpDate(org.getCreatedAt())
            .orgLastUpdated(org.getUpdatedAt())
            .orgDeletedDate(org.getEndedAt())
            .subscriptionStatus(true)
            .subscriptionPlan(subscription.getSubscriptionPlan().getPlan().toString())
            .subscriptionStartDate(subscription.getCreatedAt())
            .subscriptionEndDate(subscription.getEndedAt())
            .build();
    }
    @Transactional
    public void revokeApiKey(Long orgId, Long keyId) {
        ApiKey key = apiKeyRepo.findById(keyId)
                .orElseThrow(() -> new EntityNotFoundException("Key not found"));

        if (!key.getOrgId().equals(orgId)) {
            throw new AccessDeniedException("Unauthorized access to API key");
        }

        key.setEndedAt(LocalDateTime.now()); // Soft delete
        apiKeyRepo.save(key);

        cacheService.evictApiKey(key.getPrefix());
    }

}