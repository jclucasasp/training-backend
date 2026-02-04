package org.lucas.arbackend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.BadRequestException;
import org.jspecify.annotations.NonNull;
import org.lucas.arbackend.entity.security.ApiKey;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.lucas.arbackend.repository.security.ApiKeyRepository;
import org.lucas.arbackend.util.CustomUserDetails;
import org.lucas.arbackend.util.TenantContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepo;
    private final PasswordEncoder encoder;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String apiKeyHeader = request.getHeader("X-API-KEY");

        try {
            // 1. Check for API KEY (Student / AR App)
            if (apiKeyHeader != null && apiKeyHeader.length() > 12) {
                String prefix = apiKeyHeader.substring(0 , 12);

                // Lookup the API Key in the DB
                ApiKey apiKey = apiKeyRepo.findByPrefix(prefix)
                    .orElseThrow(() -> new BadRequestException("Invalid API Key"));

                // Validate the API Key
                if (!encoder.matches(apiKeyHeader, apiKey.getHashKey())) {
                    throw new AccessDeniedException("Invalid API Key");
                }

                // Set the current tenant for this request
                TenantContext.setCurrentTenant(apiKey.getOrganisation().getId());

                // Manually authenticate the Student for this request
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        "STUDENT_APP", null, List.of(new SimpleGrantedAuthority(RoleTypes.STUDENT.name())));
                SecurityContextHolder.getContext().setAuthentication(auth);

            }
            // 2. Check for Staff Authentication (Already logged in via Basic/JWT)
            else if (SecurityContextHolder.getContext().getAuthentication() != null) {
                Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

                if (principal instanceof CustomUserDetails user) {
                    TenantContext.setCurrentTenant(user.getOrgId());
                }
            }

            else if (apiKeyHeader != null && apiKeyHeader.length() < 12) {
                throw new BadRequestException("Malformed API Key");
            }

            filterChain.doFilter(request, response);
        } finally {
            // CRITICAL: Always clear the context to prevent memory leaks or tenant bleeding
            TenantContext.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        return request.getRequestURI().startsWith("/v3/api-docs")
                || request.getRequestURI().startsWith("/swagger-ui")
                || request.getRequestURI().startsWith("/api/v1/auth")
                || request.getRequestURI().startsWith("/api/v1/organisations")
                || request.getRequestURI().startsWith("/api/v1/health");
    }

}
