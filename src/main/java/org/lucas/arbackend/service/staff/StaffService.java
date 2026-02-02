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

    public StaffResponse createStaff(Long orgId, CreateStaffRequest request) {
        Organisation org = orgRepo.findById(orgId)
                .orElseThrow(() -> new EntityNotFoundException("Organisation not found"));

        Role role = roleRepo.findByName(request.getRole())
                .orElseThrow(() -> new EntityNotFoundException("Invalid Role"));

        Staff staff = new Staff();
        staff.setOrganisation(org);
        staff.setEmail(request.getEmail());
        staff.setPassword(passwordEncoder.encode(request.getPassword()));
        staff.setRole(role);
        staff.setActive(true);

        Staff saved = staffRepo.save(staff);

        return StaffResponse.builder()
                .id(saved.getId())
                .email(saved.getEmail())
                .role(saved.getRole().getName())
                .isActive(saved.isActive())
                .build();
    }
}
