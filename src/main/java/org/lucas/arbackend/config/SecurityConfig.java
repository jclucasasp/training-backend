package org.lucas.arbackend.config;

import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

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
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public signup/login
                        .requestMatchers(
                                "/api/v1/organisations/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**",
                                "/swagger-resources/**"
                        )
                        .permitAll()

                        // Admin & Staff Endpoints (Must be logged in)
                        .requestMatchers("/api/v1/admin/staff/**").hasAuthority(RoleTypes.ORG_ADMIN.name())
                        .requestMatchers("/api/v1/admin/course/**").hasAnyAuthority(RoleTypes.ORG_ADMIN.name(), RoleTypes.COURSE_EDITOR.name())
                        // Student Endpoints (Must have API Key via Filter)
                        .requestMatchers("/api/v1/courses/**").hasAuthority(RoleTypes.STUDENT.name())

                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults()) // Organisation & Staff login
                .addFilterAfter(tenantFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

}
