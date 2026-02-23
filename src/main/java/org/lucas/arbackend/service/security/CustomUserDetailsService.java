package org.lucas.arbackend.service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.dto.CacheDto;
import org.lucas.arbackend.util.CustomUserDetails;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
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
               auth.getId(),
                auth.getEmail(),
                auth.getPassword(),
                auth.getOrgId(),
                auth.getRoleName()
       );

    }

}
