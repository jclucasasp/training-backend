package org.lucas.arbackend.config;

import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.repository.OrganisationRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final OrganisationRepository orgRepo;
    private final StaffRepository staffRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 1. Try to find an Organisation Owner
        Optional<Organisation> org = orgRepo.findByEmail(email);
        if (org.isPresent()) {
            return User.builder()
                    .username(org.get().getEmail())
                    .password(org.get().getPassword())
                    .roles("ORG_OWNER")
                    .build();
        }

        // 2. Try to find a Staff member
        Optional<Staff> staff = staffRepo.findByEmail(email);
        if (staff.isPresent()) {
            return User.builder()
                    .username(staff.get().getEmail())
                    .password(staff.get().getPassword())
                    .roles(staff.get().getRole().getName().replace("ROLE_", ""))
                    .build();
        }

        throw new UsernameNotFoundException("User not found: " + email);
    }

}
