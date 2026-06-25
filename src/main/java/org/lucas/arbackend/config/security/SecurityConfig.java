package org.lucas.arbackend.config.security;

import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.config.filter.TenantFilter;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.lucas.arbackend.exception.CustomAuthenticationExceptionHandler;
import org.lucas.arbackend.service.cache.CacheService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TenantFilter tenantFilter;
    private final CacheService cacheService;
    private final CustomAuthenticationExceptionHandler authEntryPointExceptionHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e.authenticationEntryPoint(authEntryPointExceptionHandler))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // Organisation specific endpoints (Must be logged in)
                        .requestMatchers("/api/v1/organisation/details", "/api/v1/organisation/update")
                        .hasAnyAuthority(RoleTypes.INACTIVE.name(), RoleTypes.ORG_ADMIN.name())

                        // Admin & Staff Endpoints (Must be logged in)
                        .requestMatchers("/api/v1/admin/**").hasAuthority(RoleTypes.ORG_ADMIN.name())
                        .requestMatchers("/api/v1/staff/**").hasAnyAuthority(RoleTypes.ORG_ADMIN.name(), RoleTypes.COURSE_EDITOR.name(), RoleTypes.SUPPORT.name())
                        .requestMatchers("/api/v1/student/**").hasAnyAuthority(RoleTypes.ORG_ADMIN.name(), RoleTypes.COURSE_EDITOR.name(), RoleTypes.SUPPORT.name(), RoleTypes.STUDENT.name())
                        // Student Endpoints (Must have API Key via Filter)
                        .requestMatchers("/api/v1/courses/**").authenticated()

                        // Public signup/login and payments
                        .requestMatchers(HttpMethod.POST, "/api/v1/organisation/signup").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/payments/itn").permitAll()

                        // Other public routes
                        .requestMatchers(
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/swagger-resources/**"
                        ).permitAll()

                        .anyRequest().authenticated()
                )
                .logout(logout -> logout
                        .logoutUrl("/api/v1/auth/logout")
                        .addLogoutHandler((request, response, authentication) ->{
                            if (authentication != null && authentication.getName() != null) {
                                cacheService.evictAuthUser(authentication.getName());
                            }
                        })
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler((request, response, authentication)
                                -> response.setStatus(HttpStatus.OK.value()))
                )
                .addFilterAfter(tenantFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

}
