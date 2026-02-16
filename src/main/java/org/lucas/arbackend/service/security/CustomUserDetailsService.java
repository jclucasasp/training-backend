package org.lucas.arbackend.service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.dto.CacheDto;
import org.lucas.arbackend.util.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthLookupService authLookupService;

    @Override
    public UserDetails loadUserByUsername(String email) {

       CacheDto auth = authLookupService.getAuthCacheDto(email);

       return new CustomUserDetails(
                auth.getEmail(),
                auth.getPassword(),
                auth.getOrgId(),
                auth.getRoleName()
       );

//        // 1. Try to find an Organisation Owner
//        Optional<Organisation> org = orgRepo.findByEmail(email);
//        if (org.isPresent()) {
//            return new CustomUserDetails(
//                    org.get().getEmail(),
//                    org.get().getPassword(),
//                    org.get().getId(),
//                    RoleTypes.ORG_ADMIN.name()
//                    );
//        }
//
//        // 2. Try to find a Staff member
//        Optional<Staff> staff = staffRepo.findByEmail(email);
//        if (staff.isPresent()) {
//            return new CustomUserDetails(
//                    staff.get().getEmail(),
//                    staff.get().getPassword(),
//                    staff.get().getOrganisation().getId(),
//                    staff.get().getRole().getName()
//            );
//        }

    }

}
