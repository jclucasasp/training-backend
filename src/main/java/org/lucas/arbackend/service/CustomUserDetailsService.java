package org.lucas.arbackend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.lucas.arbackend.repository.organisation.OrganisationRepository;
import org.lucas.arbackend.repository.organisation.StaffRepository;
import org.lucas.arbackend.util.CustomUserDetails;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final OrganisationRepository orgRepo;
    private final StaffRepository staffRepo;

    @Caching(
            cacheable = {
                    @Cacheable(value = "org_users", key = "#email",
                            condition = "#result != null && #result.role == 'ORG_ADMIN'"),
                    @Cacheable(value = "staff_users", key = "#email",
                            condition = "#result != null && #result.role != 'ORG_ADMIN'")
            }
    )
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        // 1. Try to find an Organisation Owner
        Optional<Organisation> org = orgRepo.findByEmail(email);
        if (org.isPresent()) {
            return new CustomUserDetails(
                    org.get().getEmail(),
                    org.get().getPassword(),
                    org.get().getId(),
                    RoleTypes.ORG_ADMIN.name()
                    );
        }

        // 2. Try to find a Staff member
        Optional<Staff> staff = staffRepo.findByEmail(email);
        if (staff.isPresent()) {
            return new CustomUserDetails(
                    staff.get().getEmail(),
                    staff.get().getPassword(),
                    staff.get().getOrganisation().getId(),
                    staff.get().getRole().getName()
            );
        }

        throw new UsernameNotFoundException("User not found: " + email);
    }

}
