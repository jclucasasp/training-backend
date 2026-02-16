package org.lucas.arbackend.service.staff;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.organisation.StaffRequest;
import org.lucas.arbackend.dto.organisation.StaffResponse;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.security.Role;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.StaffRepository;
import org.lucas.arbackend.repository.security.RoleRepository;
import org.lucas.arbackend.service.cache.CacheService;
import org.lucas.arbackend.util.CustomUserDetails;
import org.lucas.arbackend.util.TenantProvider;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepo;
    private final RoleRepository roleRepo;
    private final OrganisationRepository orgRepo;
    private final PasswordEncoder passwordEncoder;
    private final CacheService cacheService;
    private final TenantProvider tenantProvider;

    @Cacheable(value = "staff_users", key = "#request.getEmail()")
    public StaffResponse createStaff(StaffRequest request) {

        Organisation org = tenantProvider.get();

        Role role = roleRepo.findByName(request.getRole())
                .orElseThrow(() -> new EntityNotFoundException("Invalid Role"));

        Staff staff = new Staff();
        staff.setFirstName(request.getFirstName());
        staff.setLastName(request.getLastName());
        staff.setContactNumber(request.getContactNumber());
        staff.setEmail(request.getEmail());
        staff.setPassword(passwordEncoder.encode(request.getPassword()));
        staff.setRole(role);

        staff.setOrganisation(org);

        Staff savedStaff = staffRepo.save(staff);

        CustomUserDetails newUser = new CustomUserDetails(org.getEmail(), "", org.getId(), org.getRole().getName());
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(newUser, null, newUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        return mapStaffToResponse(savedStaff);
    }

    public Page<StaffResponse> getAllStaff (Pageable pageable) {

        Organisation org = tenantProvider.get();

        return staffRepo.findAllByOrganisationIdAndEndedAtIsNull(org.getId(), pageable)
                .map(staff -> StaffResponse.builder()
                        .id(staff.getId())
                        .firstName(staff.getFirstName())
                        .lastName(staff.getLastName())
                        .contactNumber(staff.getContactNumber())
                        .email(staff.getEmail())
                        .role(staff.getRole().getName())
                        .createdAt(staff.getCreatedAt())
                        .updatedAt(staff.getUpdatedAt())
                        .build()
                );
    }

    @CachePut(value = "staff", key = "#result.email")
    public StaffResponse updateStaff (Long staffId, StaffRequest request) {
        Organisation org = tenantProvider.get();

        if (staffId == null) {
                throw new IllegalStateException("Must provide a valid organisation id and staff id");
        }

        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new EntityNotFoundException("Staff not found"));

        if (!staff.getOrganisation().getId().equals(org.getId())) {
            throw new AccessDeniedException("You are not allowed to update this staff member");
        }

        Role role = roleRepo.findByName(request.getRole())
                .orElseThrow(() -> new EntityNotFoundException("Invalid Role"));

        staff.setFirstName(request.getFirstName().isBlank() ? staff.getFirstName() : request.getFirstName());
        staff.setLastName(request.getLastName().isBlank() ? staff.getLastName() : request.getLastName());
        staff.setContactNumber(request.getContactNumber().isBlank() ? staff.getContactNumber() : request.getContactNumber());
        staff.setEmail(request.getEmail().isBlank() ? staff.getEmail() : request.getEmail());
        staff.setPassword(request.getPassword().isBlank() ? staff.getPassword() : passwordEncoder.encode(request.getPassword()));
        staff.setRole(request.getRole().isBlank() ? staff.getRole() : role);

        staffRepo.save(staff);

        return mapStaffToResponse(staff);
    }

    public void softDeleteStaff(Long staffId) {
        Organisation org = tenantProvider.get();

        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new EntityNotFoundException("Staff not found"));

        if (!staff.getOrganisation().getId().equals(org.getId()))
            throw new AccessDeniedException("You are not allowed to delete this staff member");

        cacheService.evictStaff(staff.getEmail());

        staffRepo.delete(staff);
    }

    private StaffResponse mapStaffToResponse(Staff staff) {
            return StaffResponse.builder()
                .id(staff.getId())
                .firstName(staff.getFirstName())
                .lastName(staff.getLastName())
                .contactNumber(staff.getContactNumber())
                .email(staff.getEmail())
                .role(staff.getRole().getName())
                .createdAt(staff.getCreatedAt())
                .updatedAt(staff.getUpdatedAt())
                .build();
    }

}
