package org.lucas.arbackend.config;

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
public class SecurityConfig {
// TODO: enable once jwt has been added to maven
//    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public signup/login
                        .requestMatchers("/api/v1/organisations/**",
                            "/api/v1/auth/**", "/v3/api-docs/**",
                            "/swagger-ui/**", "/swagger-ui.html")
                        .permitAll()

                        // Strict Role Enforcement
                        .requestMatchers("/api/v1/admin/**").hasRole(RoleTypes.ORG_ADMIN.name())
                        .requestMatchers("/api/v1/courses/**").hasAnyRole(RoleTypes.ORG_ADMIN.name(), RoleTypes.COURSE_EDITOR.name())

                        .anyRequest().authenticated()
                )
                // TODO: enable once jwt has been added to maven
                // Add JWT filter before the standard username/password filter
//                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
                // TODO: Disable once jwt has been added to maven
                .httpBasic(withDefaults()); // Change to .oauth2ResourceServer() if using JWT later

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // TODO: enable once jwt has been added to maven
//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
//        return config.getAuthenticationManager();
//    }
}
