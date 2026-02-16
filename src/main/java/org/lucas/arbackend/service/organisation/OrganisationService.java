package org.lucas.arbackend.service.organisation;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.dto.organisation.OrganisationRequest;
import org.lucas.arbackend.dto.organisation.OrganisationResponse;
import org.lucas.arbackend.dto.security.ApiKeyResponse;
import org.lucas.arbackend.entity.Organisation.OrgAddress;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.OrganisationSubscription;
import org.lucas.arbackend.entity.Organisation.Profile;
import org.lucas.arbackend.entity.PlanTypes;
import org.lucas.arbackend.entity.Organisation.SubscriptionPlan;
import org.lucas.arbackend.entity.security.ApiKey;
import org.lucas.arbackend.entity.security.Role;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.lucas.arbackend.repository.organisation.SubscriptionPlanRepository;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.security.ApiKeyRepository;
import org.lucas.arbackend.repository.security.RoleRepository;
import org.lucas.arbackend.service.security.ApiKeyService;
import org.lucas.arbackend.service.cache.CacheService;
import org.lucas.arbackend.util.CustomUserDetails;
import org.lucas.arbackend.util.TenantProvider;
import org.springframework.cache.annotation.CachePut;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final SubscriptionPlanRepository planRepo;
    private final ApiKeyRepository apiKeyRepo;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepo;
    private final TenantProvider tenantProvider;

    private final ApiKeyService apiKeyService;
    private final CacheService cacheService;

    // TODO: Implement a check to make sure a company can not generate more then one API key. Implement a new method to be able to end the old one and generate a new one.
    // ==========================================
    // 1. ATOMIC SIGN UP (Org + Profile + Sub)
    // ==========================================
    public OrganisationResponse signup(OrganisationRequest request) {
        // 1. Validation & Role Lookup
        if (orgRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }

        Role role = roleRepo.findByName(RoleTypes.ORG_ADMIN.name())
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        // 2. Create Organisation
        Organisation org = new Organisation();
        org.setFirstName(request.getFirstName());
        org.setLastName(request.getLastName());
        org.setContactNumber(request.getContactNumber());
        org.setEmail(request.getEmail());
        org.setPassword(passwordEncoder.encode(request.getPassword()));
        org.setRole(role);

        // 3. Create Subscription & Link
        SubscriptionPlan plan = planRepo.findById(request.getInitialPlanId())
                .orElseThrow(() -> new EntityNotFoundException("Plan not found"));
        OrganisationSubscription subscription = new OrganisationSubscription();
        subscription.setSubscriptionPlan(plan);
        subscription.setStatus(1);
        subscription.setEndedAt(plan.getPlan().toString().equals(PlanTypes.MONTHLY.name()) ?
                LocalDateTime.now().plusMonths(1) : LocalDateTime.now().plusMonths(12));

        // CRITICAL: Bi-directional link for @MapsId
        subscription.setOrganisation(org);
        org.setSubscription(subscription);

        // 4. Create Profile & Address & Link
        Profile profile = new Profile();
        profile.setOrgName(request.getOrgName());
        profile.setRegistrationNumber(request.getRegistrationNumber());
        profile.setVatNumber(request.getVatNumber());

        OrgAddress address = new OrgAddress();
        address.setStreet(request.getStreet());
        address.setSuburb(request.getSuburb());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZip(request.getZip());

        ApiKey apiKey = new ApiKey();
        ApiKeyResponse apiKeyResponse = apiKeyService.generateKeyForOrg(apiKey);

        if (apiKeyResponse.getRawKey().isBlank()) {
            throw new RuntimeException("API Key could not be generated");
        }

        apiKey.setOrganisation(org);
        address.setProfile(profile);
        profile.setAddress(address); // Assuming Profile has cascade = CascadeType.ALL for address

        profile.setOrganisation(org);
        org.setProfile(profile);
        org.setApiKey(apiKey);

        // 5. THE SINGLE SAVE
        // Persists Org, Subscription, Profile, and Address in one transaction
        Organisation savedOrg = orgRepo.save(org);

        CustomUserDetails newUser = new CustomUserDetails(org.getEmail(), "", org.getId(), org.getRole().getName());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(newUser, null, newUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        return mapToOrganisationResponse(savedOrg);

    }

    // ==========================================
    // 2. PROFILE MANAGEMENT
    // ==========================================
    // TODO: Implement a function to update the OrganisationSubscription entity
    @CachePut(value = "org_users", key = "#request.getEmail()")
    public OrganisationResponse updateProfile(OrganisationRequest req) {

        Organisation org = tenantProvider.get();

        Profile profile = org.getProfile();
        OrgAddress address = org.getProfile().getAddress();

        // Update the Organisation
        if (req.getEmail().isBlank()) org.setEmail(req.getEmail());
        if (req.getPassword().isBlank()) org.setPassword(passwordEncoder.encode(req.getPassword()));

        // Update the Profile
        if (req.getOrgName().isBlank()) profile.setOrgName(req.getOrgName());
        if (req.getRegistrationNumber().isBlank()) profile.setRegistrationNumber(req.getRegistrationNumber());
        if (req.getVatNumber().isBlank()) profile.setVatNumber(req.getVatNumber());

        // Update the Address
        if (req.getContactNumber() != null) org.setContactNumber(req.getContactNumber());
        if (req.getFirstName().isBlank()) org.setFirstName(req.getFirstName());
        if (req.getLastName().isBlank()) org.setLastName(req.getLastName());
        if (req.getContactNumber() != null) org.setContactNumber(req.getContactNumber());
        if (req.getStreet().isBlank()) address.setStreet(req.getStreet());
        if (req.getSuburb().isBlank()) address.setSuburb(req.getSuburb());
        if (req.getCity().isBlank()) address.setCity(req.getCity());
        if (req.getState().isBlank()) address.setState(req.getState());
        if (req.getZip() != null) address.setZip(req.getZip());

        // No need to call save() if @Transactional is active,
        // Hibernate dirty checking handles it, but explicit save is fine too.
        orgRepo.save(org);

        return mapToOrganisationResponse(org);
    }

    // This tells the database that it will just be a lookup which speed things up by not doing dirty checking or object snapshots, flushing
    @Transactional(readOnly = true)
    public OrganisationResponse getOrganisationDetails() {

        Organisation org = tenantProvider.get();
        return mapToOrganisationResponse(org);
    }

    public void softDeleteOrg() {

        Organisation org = tenantProvider.get();

        cacheService.evictOrganisation(org.getEmail());
        orgRepo.delete(org);
    }

    public void revokeApiKey(Long keyId) {

        Organisation org = tenantProvider.get();

        ApiKey key = apiKeyRepo.findById(keyId)
                .orElseThrow(() -> new EntityNotFoundException("Key not found"));

        if (!key.getOrgId().equals(org.getId())) {
            throw new AccessDeniedException("Unauthorized access to API key");
        }

        cacheService.evictApiKey(key.getPrefix());
        apiKeyRepo.delete(key);
    }

    private OrganisationResponse mapToOrganisationResponse(Organisation org) {

        return OrganisationResponse.builder()
                .id(org.getId())
                .orgName(org.getProfile().getOrgName())
                .firstName(org.getFirstName())
                .lastName(org.getLastName())
                .contactNumber(org.getContactNumber())
                .email(org.getEmail())
                .registrationNumber(org.getProfile().getRegistrationNumber())
                .vatNumber(org.getProfile().getVatNumber())
                .streetAddress(org.getProfile().getAddress().getStreet())
                .suburb(org.getProfile().getAddress().getSuburb())
                .city(org.getProfile().getAddress().getCity())
                .state(org.getProfile().getAddress().getState())
                .zip(org.getProfile().getAddress().getZip())
                .apiKey(org.getApiKey().getHashKey())
                .createdAt(org.getCreatedAt())
                .updatedAt(org.getUpdatedAt())
                .endedAt(org.getEndedAt())
                .subscriptionStatus(org.getSubscription().getStatus() == 1)
                .subscriptionPlan(org.getSubscription().getSubscriptionPlan().getPlan().toString())
                .subscriptionStartDate(org.getSubscription().getCreatedAt())
                .subscriptionEndDate(org.getSubscription().getEndedAt())
                .build();

    }

}