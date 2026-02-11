package org.lucas.arbackend.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.dto.organisation.OrgDetailsRequest;
import org.lucas.arbackend.dto.organisation.OrganisationResponse;
import org.lucas.arbackend.dto.security.ApiKeyResponse;
import org.lucas.arbackend.entity.Organisation.OrgAddress;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.OrganisationSubscription;
import org.lucas.arbackend.entity.Organisation.Profile;
import org.lucas.arbackend.entity.PlanTypes;
import org.lucas.arbackend.entity.SubscriptionPlan;
import org.lucas.arbackend.entity.security.ApiKey;
import org.lucas.arbackend.entity.security.Role;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.lucas.arbackend.repository.SubscriptionPlanRepository;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.security.ApiKeyRepository;
import org.lucas.arbackend.repository.security.RoleRepository;
import org.lucas.arbackend.util.CustomUserDetails;
import org.lucas.arbackend.util.TenantContext;
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

    private final ApiKeyService apiKeyService;

    // TODO: Implement a check to make sure a company can not generate more then one API key. Implement a new method to be able to end the old one and generate a new one.
    // ==========================================
    // 1. ATOMIC SIGN UP (Org + Profile + Sub)
    // ==========================================
    public OrganisationResponse signup(OrgDetailsRequest request) {
        // 1. Validation & Role Lookup
        if (orgRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }

        Role role = roleRepo.findByName(RoleTypes.ORG_ADMIN.name())
                .orElseThrow(() -> new EntityNotFoundException("Role not found"));

        // 2. Create Organisation
        Organisation org = new Organisation();
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
        profile.setContactPersonFirstName(request.getContactPersonFirstName());
        profile.setContactPersonLastName(request.getContactPersonLastName());
        profile.setContactNumber(request.getContactNumber());

        OrgAddress address = new OrgAddress();
        address.setStreet(request.getStreet());
        address.setSuburb(request.getSuburb());
        address.setCity(request.getCity());
        address.setState(request.getState());
        address.setZip(request.getZip());

        // CRITICAL: Link Address to Profile AND Profile to Org
        address.setProfile(profile);
        profile.setAddress(address); // Assuming Profile has cascade = CascadeType.ALL for address

        profile.setOrganisation(org);
        org.setProfile(profile);

        // 5. THE SINGLE SAVE
        // Persists Org, Subscription, Profile, and Address in one transaction
        Organisation savedOrg = orgRepo.save(org);

        // 6. Generate API Key
        ApiKeyResponse apiKeyResponse = apiKeyService.generateKeyForOrg(savedOrg.getId());

        if (apiKeyResponse.getRawKey().isBlank()) {
            throw new RuntimeException("API Key could not be generated");
        }

        // TODO: Implement this in the StaffService as well
        CustomUserDetails newUser = new CustomUserDetails(org.getEmail(), "", org.getId(), org.getRole().getName());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(newUser, null, newUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        return OrganisationResponse.builder()
                .id(org.getId())
                .email(org.getEmail())
                .orgName(profile.getOrgName())
                .registrationNumber(profile.getRegistrationNumber())
                .vatNumber(profile.getVatNumber())
                .contactPersonFirstName(profile.getContactPersonFirstName())
                .contactPersonLastName(profile.getContactPersonLastName())
                .contactNumber(profile.getContactNumber())
                .streetAddress(address.getStreet())
                .suburb(address.getSuburb())
                .city(address.getCity())
                .state(address.getState())
                .zip(address.getZip())
                .apiKey(apiKeyResponse.getRawKey())
                .orgSignedUpDate(org.getCreatedAt())
                .orgLastUpdated(org.getUpdatedAt())
                .orgDeletedDate(org.getEndedAt())
                .subscriptionStatus(subscription.getStatus() == 1)
                .subscriptionPlan(plan.getPlan().toString())
                .subscriptionStartDate(subscription.getSubscribedAt())
                .subscriptionEndDate(subscription.getExpiresAt())
                .build();

    }

    // ==========================================
    // 2. PROFILE MANAGEMENT
    // ==========================================
    // TODO: Implement a function to update the OrganisationSubscription entity
    public void updateProfile(OrgDetailsRequest req) {

        Long orgId = getTenantId();

        Organisation org = orgRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found in the Tenant Context"));

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
        if (req.getContactNumber() != null) profile.setContactNumber(req.getContactNumber());
        if (req.getContactPersonFirstName().isBlank()) profile.setContactPersonFirstName(req.getContactPersonFirstName());
        if (req.getContactPersonLastName().isBlank()) profile.setContactPersonLastName(req.getContactPersonLastName());
        if (req.getStreet().isBlank()) address.setStreet(req.getStreet());
        if (req.getSuburb().isBlank()) address.setSuburb(req.getSuburb());
        if (req.getCity().isBlank()) address.setCity(req.getCity());
        if (req.getState().isBlank()) address.setState(req.getState());
        if (req.getZip() != null) address.setZip(req.getZip());

        // No need to call save() if @Transactional is active,
        // Hibernate dirty checking handles it, but explicit save is fine too.
        orgRepo.save(org);
    }

    // This tells the database that it will just be a lookup which speed things up by not doing dirty checking or object snapshots, flushing
    @Transactional(readOnly = true)
    public OrganisationResponse getOrganisationDetails() {

        Long orgId = getTenantId();

        Organisation org = orgRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        Profile profile = org.getProfile();
        log.info("Profile found: [{}]", profile);

        OrgAddress address = org.getProfile().getAddress();
        log.info("Address found: [{}]", address);

        OrganisationSubscription subscription = org.getSubscription();
        log.info("Subscription found: [{}]", subscription);

        return OrganisationResponse.builder()
                .id(org.getId())
                .email(org.getEmail())
                .orgName(profile.getOrgName())
                .registrationNumber(profile.getRegistrationNumber())
                .vatNumber(profile.getVatNumber())
                .contactPersonFirstName(profile.getContactPersonFirstName())
                .contactPersonLastName(profile.getContactPersonLastName())
                .contactNumber(profile.getContactNumber())
                .streetAddress(address.getStreet())
                .suburb(address.getSuburb())
                .city(address.getCity())
                .state(address.getState())
                .zip(address.getZip())
                .apiKey(profile.getApiKey().getHashKey())
                .orgSignedUpDate(org.getCreatedAt())
                .orgLastUpdated(org.getUpdatedAt())
                .orgDeletedDate(org.getEndedAt())
                .subscriptionStatus(subscription.getStatus() == 1)
                .subscriptionPlan(subscription.getSubscriptionPlan().getPlan().toString())
                .subscriptionStartDate(subscription.getCreatedAt())
                .subscriptionEndDate(subscription.getEndedAt())
                .build();
    }

    public void softDeleteOrg() {

        Long orgId = getTenantId();

        Organisation org = orgRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        orgRepo.delete(org);
    }

    public void revokeApiKey(Long keyId) {

        Long orgId = getTenantId();

        ApiKey key = apiKeyRepo.findById(keyId)
                .orElseThrow(() -> new EntityNotFoundException("Key not found"));

        if (!key.getOrgId().equals(orgId)) {
            throw new AccessDeniedException("Unauthorized access to API key");
        }

        apiKeyRepo.delete(key);
    }

    private Long getTenantId() {
        Long orgId = TenantContext.getCurrentTenant();

        if (orgId == null) {
            throw new IllegalStateException("No Organisation id found in the Tenant Context");
        }

        return orgId;
    }

}