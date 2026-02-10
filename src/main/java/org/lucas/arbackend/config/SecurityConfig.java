package org.lucas.arbackend.config;

import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextHolderFilter;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TenantFilter tenantFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(auth -> auth
                        // Organisation specific endpoints (Must be logged in)
                        .requestMatchers("/api/v1/organisations/details").hasAuthority(RoleTypes.ORG_ADMIN.name())
                        .requestMatchers("/api/v1/organisations/profile").hasAuthority(RoleTypes.ORG_ADMIN.name())
                        .requestMatchers("/api/v1/organisations/api-keys").hasAuthority(RoleTypes.ORG_ADMIN.name())

                        // Admin & Staff Endpoints (Must be logged in)
                        .requestMatchers("/api/v1/admin/staff/**").hasAuthority(RoleTypes.ORG_ADMIN.name())
                        .requestMatchers("/api/v1/admin/course/**").hasAnyAuthority(RoleTypes.ORG_ADMIN.name(), RoleTypes.COURSE_EDITOR.name())
                        // Student Endpoints (Must have API Key via Filter)
                        .requestMatchers("/api/v1/courses/**").hasAnyAuthority(RoleTypes.ORG_ADMIN.name(), RoleTypes.COURSE_EDITOR.name(), RoleTypes.SUPPORT.name(), RoleTypes.STUDENT.name())

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
