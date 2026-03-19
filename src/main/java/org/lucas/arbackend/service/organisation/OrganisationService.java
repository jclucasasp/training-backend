package org.lucas.arbackend.service.organisation;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.dto.organisation.OrganisationRequest;
import org.lucas.arbackend.dto.organisation.OrganisationResponse;
import org.lucas.arbackend.dto.security.ApiKeyResponse;
import org.lucas.arbackend.entity.Organisation.*;
import org.lucas.arbackend.entity.PlanTypes;
import org.lucas.arbackend.entity.security.ApiKey;
import org.lucas.arbackend.entity.security.Role;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.lucas.arbackend.mapper.OrganisationMapper;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.SubscriptionPlanRepository;
import org.lucas.arbackend.repository.security.ApiKeyRepository;
import org.lucas.arbackend.repository.security.RoleRepository;
import org.lucas.arbackend.service.cache.CacheService;
import org.lucas.arbackend.service.security.ApiKeyService;
import org.lucas.arbackend.util.CustomUserDetails;
import org.lucas.arbackend.util.tenant.TenantProvider;
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

    private final OrganisationMapper orgMapper;

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

        orgMapper.updateOrganisation(request, org);
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
        orgMapper.updateProfile(request, profile);

        OrgAddress address = new OrgAddress();
        orgMapper.updateAddress(request, address);

        ApiKey apiKey = new ApiKey();
        ApiKeyResponse apiKeyResponse = apiKeyService.generateKeyForOrg(apiKey, true);

        if (apiKeyResponse.getRawKey().isBlank()) {
            throw new RuntimeException("API Key could not be generated");
        }

        apiKey.setOrganisation(org);
        address.setProfile(profile);
        profile.setAddress(address); //  CascadeType.ALL for address

        profile.setOrganisation(org);
        org.setProfile(profile);
        org.setApiKey(apiKey);

        // 5. THE SINGLE SAVE
        // Persists Org, Subscription, Profile, and Address in one transaction
        Organisation savedOrg = orgRepo.save(org);

        CustomUserDetails newUser = new CustomUserDetails(org.getId(), org.getEmail(), "", org.getId(), org.getRole().getName());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(newUser, null, newUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        return orgMapper.mapToOrgResponse(savedOrg, apiKeyResponse.getRawKey());

    }

    // ==========================================
    // 2. PROFILE MANAGEMENT
    // ==========================================
/**
 * Updates the organization profile with the provided request data.
 * This method handles the update of organization information, profile details,
 * and address. It also includes special handling for password changes and email updates.
 *
 * @param req The request object containing updated organization information
 * @return OrganisationResponse containing the updated organization data
 */
    // TODO: Implement a function to update the OrganisationSubscription entity
    public OrganisationResponse updateProfile(OrganisationRequest req) {

    // Retrieve the organization entity
        Organisation org = findOrganisation();

    // Get the profile and address entities from the organization
        Profile profile = org.getProfile();
        OrgAddress address = org.getProfile().getAddress();

        // Automated Mapping
        orgMapper.updateOrganisation(req, org);
        orgMapper.updateProfile(req, profile);
        orgMapper.updateAddress(req, address);

        // Special logic for password (still needs manual encoding)
        if (req.getPassword() != null) {
            org.setPassword(passwordEncoder.encode(req.getPassword()));
        }

        if (req.getEmail() != null) {
            cacheService.evictAuthUser(org.getEmail());
        }

        // No need to call save() if @Transactional is active,
        // Hibernate dirty checking handles it, but explicit save is fine too.
        orgRepo.save(org);
        cacheService.updateCache("org_user", org.getEmail(), orgMapper.mapToOrgResponse(org));

        return orgMapper.mapToOrgResponse(org);
    }

    // This tells the database that it will just be a lookup which speed things up by not doing dirty checking or object snapshots, flushing
    @Transactional(readOnly = true)
    public OrganisationResponse getOrganisationDetails() {

        Organisation org = findOrganisation();
        return orgMapper.mapToOrgResponse(org);
    }

    public void softDeleteOrg(Long orgId) {

        Organisation org = orgRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        cacheService.evictAuthUser(org.getEmail());
        orgRepo.delete(org);
    }

    private Organisation findOrganisation() {
        Long orgId = tenantProvider.get();

        return orgRepo.findById(tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("No organisation found for tenant id: [" + orgId + "]"));
    }

}