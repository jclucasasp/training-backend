package org.lucas.arbackend.service.staff;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.lucas.arbackend.dto.organisation.StaffRequest;
import org.lucas.arbackend.dto.organisation.StaffResponse;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.security.Role;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.lucas.arbackend.mapper.StaffMapper;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.StaffRepository;
import org.lucas.arbackend.repository.security.RoleRepository;
import org.lucas.arbackend.service.cache.CacheService;
import org.lucas.arbackend.util.tenant.TenantProvider;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
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
    private final StaffMapper staffMapper;

    @Cacheable(value = "staff_user", key = "#request.getEmail()")
    public StaffResponse createStaff(StaffRequest request) {

        Organisation org = findOrganisation();

        Role role = roleRepo.findByRoleName(request.getRole());

        Staff staff = new Staff();
        staffMapper.updateStaff(request, staff);

        staff.setPassword(passwordEncoder.encode(request.getPassword()));
        staff.setRole(role);

        staff.setOrganisation(org);

        Staff savedStaff = staffRepo.save(staff);

        return staffMapper.maptoStaffResponse(savedStaff);
    }

    public Page<StaffResponse> getAllStaff (Pageable pageable) {

        Organisation org = findOrganisation();

        return staffRepo.findAllByOrganisationId(org.getId(), pageable)
                .map(staffMapper::maptoStaffResponse);
    }

    public StaffResponse updateStaffDetails(Long staffId, StaffRequest request) {

        if (staffId == null) {
                throw new IllegalStateException("Must provide a valid organisation id and staff id");
        }

        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new EntityNotFoundException("Staff not found"));

        if (!staff.getOrganisation().getId().equals(tenantProvider.get())) {
            throw new AccessDeniedException("You are not allowed to update this staff member");
        }

        staffMapper.updateStaff(request, staff);
        staff.setPassword(request.getPassword() == null ? staff.getPassword() : passwordEncoder.encode(request.getPassword()));

        if (request.getEmail() != null) {
            cacheService.evictAuthUser(staff.getEmail());
        }

        StaffResponse staffResponse = staffMapper.maptoStaffResponse(staff);

        cacheService.updateCache("staff_user", staff.getEmail(), staffResponse);

        staffRepo.save(staff);

        return staffResponse;
    }

    @CachePut(value = "staff_user", key = "#result.email")
    public StaffResponse updateStaffRole(Long staffId, RoleTypes role) throws BadRequestException {

        if (staffId == null) {
            throw new IllegalStateException("Must provide a valid organisation id and staff id");
        }

        Role newRole = roleRepo.findByRoleName(role);

        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new EntityNotFoundException("Staff member not found"));

        if (!staff.getOrganisation().getId().equals(tenantProvider.get())) {
            throw new AccessDeniedException("You are not allowed to update this staff member");
        }

        staff.setRole(newRole);

        staffRepo.save(staff);

        return staffMapper.maptoStaffResponse(staff);
    }

    public void softDeleteStaff(Long staffId) {
        Organisation org = findOrganisation();

        Staff staff = staffRepo.findById(staffId)
                .orElseThrow(() -> new EntityNotFoundException("Staff not found"));

        if (!staff.getOrganisation().getId().equals(org.getId()))
            throw new AccessDeniedException("You are not allowed to delete this staff member");

        cacheService.evictAuthUser(staff.getEmail());

        staffRepo.delete(staff);
    }

    private Organisation findOrganisation() {
        Long orgId = tenantProvider.get();

        return orgRepo.findById(tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("No organisation found for tenant id: [" + orgId +"]"));
    }

}
