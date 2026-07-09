package org.lucas.arbackend.service.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.dto.CacheDto;
import org.lucas.arbackend.util.CustomUserDetails;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of Spring Security's UserDetailsService interface.
 * This service is responsible for loading user-specific data during authentication.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Service for looking up authentication data from cache.
     * Injected via constructor injection using @RequiredArgsConstructor.
     */
    private final AuthLookupService authLookupService;

    /**
     * Loads the user data for the given email address.
     *
     * @param email the email address identifying the user whose data is to be loaded
     * @return a fully populated user record (never null)
     * @throws DisabledException if the user's subscription is not active
     */
    @Override
    public UserDetails loadUserByUsername(String email) {

        // Retrieve authentication data from cache using the provided email
       CacheDto auth = authLookupService.getAuthCacheDto(email);

        // Create and return a CustomUserDetails object with the retrieved authentication data
       return new CustomUserDetails(
               auth.getId(),
               auth.getEmail(),
               auth.getStudentNumber(),
               auth.getPassword(),
               auth.getOrgId(),
               auth.getRoleName()
       );

    }

}
