package org.lucas.arbackend.service.staff;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.organisation.CreateStaffRequest;
import org.lucas.arbackend.dto.organisation.StaffResponse;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.security.Role;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.StaffRepository;
import org.lucas.arbackend.repository.security.RoleRepository;
import org.lucas.arbackend.service.CacheService;
import org.lucas.arbackend.util.CustomUserDetails;
import org.lucas.arbackend.util.TenantContext;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

@Service
@Transactional
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepo;
    private final RoleRepository roleRepo;
    private final OrganisationRepository orgRepo;
    private final PasswordEncoder passwordEncoder;
    private final CacheService cacheService;

    public StaffResponse createStaff(@Validated CreateStaffRequest request) {

        Long orgId = getTenantId();

        Organisation org = orgRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        Role role = roleRepo.findByName(request.getRole())
                .orElseThrow(() -> new EntityNotFoundException("Invalid Role"));

        Staff staff = new Staff();
        staff.setOrganisation(org);
        staff.setEmail(request.getEmail());
        staff.setPassword(passwordEncoder.encode(request.getPassword()));
        staff.setRole(role);

        Staff saved = staffRepo.save(staff);

        CustomUserDetails newUser = new CustomUserDetails(org.getEmail(), "", org.getId(), org.getRole().getName());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(newUser, null, newUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        return StaffResponse.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .role(saved.getRole().getName())
                .build();
    }

    public Page<StaffResponse> getAllStaff (Pageable pageable) {
        Long orgId = getTenantId();

        return staffRepo.findAllByOrganisationIdAndEndedAtIsNull(orgId, pageable)
                .map(staff -> StaffResponse.builder()
                        .id(staff.getId())
                        .email(staff.getEmail())
                        .role(staff.getRole().getName())
                        .build()
                );
    }

    @CachePut(value = "staff", key = "#result.email")
    public StaffResponse updateStaff (Long staffId, CreateStaffRequest request) {
        Long orgId = getTenantId();

        if (staffId == null) {
                throw new IllegalStateException("Must provide a valid organisation id and staff id");
        }

        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new EntityNotFoundException("Staff not found"));

        if (!staff.getOrganisation().getId().equals(orgId)) {
            throw new AccessDeniedException("You are not allowed to update this staff member");
        }

        Role role = roleRepo.findByName(request.getRole())
                .orElseThrow(() -> new EntityNotFoundException("Invalid Role"));

        staff.setEmail(request.getEmail().isBlank() ? staff.getEmail() : request.getEmail());
        staff.setPassword(request.getPassword().isBlank() ? staff.getPassword() : passwordEncoder.encode(request.getPassword()));
        staff.setRole(request.getRole().isBlank() ? staff.getRole() : role);

        staffRepo.save(staff);

        return StaffResponse.builder()
                .id(staff.getId())
                .email(staff.getEmail())
                .role(staff.getRole().getName())
                .build();
    }

    public void softDeleteStaff(Long staffId) {
        Long orgId = getTenantId();

        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new EntityNotFoundException("Staff not found"));

        if (!staff.getOrganisation().getId().equals(orgId))
            throw new AccessDeniedException("You are not allowed to delete this staff member");

        cacheService.evictStaff(staff.getEmail());

        staffRepo.delete(staff);
    }

    private Long getTenantId() {
        Long orgId = TenantContext.getCurrentTenant();

        if (orgId == null) {
            throw new IllegalStateException("No organisation id found in the Tenant Context");
        }
        return orgId;
    }
}
