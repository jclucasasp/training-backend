package org.lucas.arbackend.config.security;

import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.config.filter.TenantFilter;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.lucas.arbackend.exception.CustomAuthenticationExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import java.lang.reflect.Method;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TenantFilter tenantFilter;
    private final CustomAuthenticationExceptionHandler authEntryPointExceptionHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(e -> e.authenticationEntryPoint(authEntryPointExceptionHandler))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // Organisation specific endpoints (Must be logged in)
                        .requestMatchers(HttpMethod.GET, "/api/v1/organisations/***").hasAuthority(RoleTypes.ORG_ADMIN.name())

                        // Admin & Staff Endpoints (Must be logged in)
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/staff/*/update/details")
                        .hasAnyAuthority(RoleTypes.ORG_ADMIN.name(), RoleTypes.COURSE_EDITOR.name(), RoleTypes.SUPPORT.name())
                        .requestMatchers(HttpMethod.PUT, "/api/v1/admin/staff/*/update/role")
                        .hasAuthority(RoleTypes.ORG_ADMIN.name())
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/admin/staff/*/delete").hasAuthority(RoleTypes.ORG_ADMIN.name())

                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/staff/**").hasAuthority(RoleTypes.ORG_ADMIN.name())
                        .requestMatchers(HttpMethod.POST, "/api/v1/admin/course/**")
                        .hasAnyAuthority(RoleTypes.ORG_ADMIN.name(), RoleTypes.COURSE_EDITOR.name())
                        // Student Endpoints (Must have API Key via Filter)
                        .requestMatchers(HttpMethod.GET, "/api/v1/courses/**").authenticated()

                        // Public signup/login
                        .requestMatchers(HttpMethod.POST, "/api/v1/organisations/signup").permitAll()

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
                .httpBasic(withDefaults()) // Organisation & Staff login
                .addFilterAfter(tenantFilter, BasicAuthenticationFilter.class);

        return httpSecurity.build();
    }

}
