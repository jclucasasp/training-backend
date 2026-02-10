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
import org.lucas.arbackend.util.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository staffRepo;
    private final RoleRepository roleRepo;
    private final OrganisationRepository orgRepo;
    private final PasswordEncoder passwordEncoder;

    public StaffResponse createStaff(CreateStaffRequest request) {

        Long orgId = TenantContext.getCurrentTenant();

        if (orgId == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No organisation found for id: [" + orgId + "]");
        }

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

        return StaffResponse.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .role(saved.getRole().getName())
                .build();
    }

    public Page<StaffResponse> getAllStaff (Pageable pageable) {
        Long orgId = TenantContext.getCurrentTenant();

        if (orgId == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No organisation found for id: [" + orgId + "]");
        }

        return staffRepo.findAllByOrganisationIdAndEndedAtIsNull(orgId, pageable)
                .map(staff -> StaffResponse.builder()
                        .id(staff.getId())
                        .email(staff.getEmail())
                        .role(staff.getRole().getName())
                        .build()
                );
    }

    public StaffResponse updateStaff (Long staffId, CreateStaffRequest request) {
        Long orgId = TenantContext.getCurrentTenant();

        if (orgId == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No organisation found for id: [" + orgId + "]");
        }

        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new EntityNotFoundException("Staff not found"));

        if (!staff.getOrganisation().getId().equals(orgId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update this staff member");
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
        Long orgId = TenantContext.getCurrentTenant();

        if (orgId == null) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No organisation found for id: [" + orgId + "]");
        }

        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new EntityNotFoundException("Staff not found"));

        if (!staff.getOrganisation().getId().equals(orgId))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this staff member");

        staffRepo.delete(staff);
    }
}
