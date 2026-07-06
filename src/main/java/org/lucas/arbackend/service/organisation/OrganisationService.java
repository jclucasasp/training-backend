package org.lucas.arbackend.service.organisation;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.dto.organisation.OrganisationRequest;
import org.lucas.arbackend.dto.organisation.OrganisationResponse;
import org.lucas.arbackend.dto.security.ApiKeyResponse;
import org.lucas.arbackend.entity.Organisation.OrgAddress;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.Profile;
import org.lucas.arbackend.entity.security.ApiKey;
import org.lucas.arbackend.entity.security.Role;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.lucas.arbackend.mapper.OrganisationMapper;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.SubscriptionPlanRepository;
import org.lucas.arbackend.repository.security.ApiKeyRepository;
import org.lucas.arbackend.repository.security.RoleRepository;
import org.lucas.arbackend.service.cache.CacheService;
import org.lucas.arbackend.service.messaging.CustomEmailType;
import org.lucas.arbackend.service.messaging.EmailProducer;
import org.lucas.arbackend.service.security.ApiKeyService;
import org.lucas.arbackend.util.CustomUserDetails;
import org.lucas.arbackend.util.OTPService;
import org.lucas.arbackend.util.tenant.TenantProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final OTPService otpService;
    private final EmailProducer emailProducer;

    private final OrganisationMapper orgMapper;

/**
 * This class handles the atomic signup process for organisations, creating the organisation,
 * profile, address, and API key in a single transaction.
 * The signup method ensures data consistency and handles role assignment appropriately.
 */
    // ==========================================
    // 1. ATOMIC SIGN UP (Org + Profile + Sub)
    // ==========================================
    public OrganisationResponse signup(OrganisationRequest request) {
        // 1. Validation & Role Lookup
    // Check if email is already registered
        if (orgRepo.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered");
        }
        // Set the role to INACTIVE because no subscription is active yet
        Role role = roleRepo.findByRoleName(RoleTypes.INACTIVE);

        // 2. Create Organisation
        Organisation org = new Organisation();

    // Map organisation details from request to entity
        orgMapper.updateOrganisation(request, org);
    // Encode and set password for security
        org.setPassword(passwordEncoder.encode(request.getPassword()));
    // Assign the inactive role to the organisation
        org.setRole(role);

        // 4. Create Profile & Address & Link
        Profile profile = new Profile();
    // Map profile details from request to entity
        orgMapper.updateProfile(request, profile);

    // Map address details from request to entity
        OrgAddress address = new OrgAddress();
        orgMapper.updateAddress(request, address);

    // Generate API key for the organisation
        ApiKey apiKey = new ApiKey();
        ApiKeyResponse apiKeyResponse = apiKeyService.generateKeyForOrg(apiKey, true);

    // Validate API key generation
        if (apiKeyResponse.getRawKey().isBlank()) {
            throw new RuntimeException("API Key could not be generated");
        }

    // Establish relationships between entities
        apiKey.setOrganisation(org);
        address.setProfile(profile);
        profile.setAddress(address); //  CascadeType.ALL for address

        profile.setOrganisation(org);
        org.setProfile(profile);
        org.setApiKey(apiKey);

        // 5. THE SINGLE SAVE
        // Persists Org, Subscription, Profile, and Address in one transaction
        Organisation savedOrg = orgRepo.save(org);

    // Create and authenticate the new user
        CustomUserDetails newUser = new CustomUserDetails(org.getId(), org.getEmail(), "", org.getId(), org.getRole().getRoleName().name());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(newUser, null, newUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

    // Publish email message to RabbitMQ for asynchronous processing
        emailProducer.queueEmail(org.getProfile().getOrgName(), org.getEmail(), null, CustomEmailType.SUBSCRIPTION_REMINDER);

    // Return the saved organisation with API key
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
    public OrganisationResponse updateProfile(OrganisationRequest req) {

    // Retrieve the organization entity
        Organisation org = tenantProvider.getOrg();

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

        Organisation org = tenantProvider.getOrg();
        return orgMapper.mapToOrgResponse(org);
    }

    public void softDeleteOrg(Long orgId) {

        Organisation org = orgRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        cacheService.evictAuthUser(org.getEmail());
        orgRepo.delete(org);
    }

}