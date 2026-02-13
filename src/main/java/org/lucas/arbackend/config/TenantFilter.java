package org.lucas.arbackend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.jspecify.annotations.NonNull;
import org.lucas.arbackend.entity.security.ApiKey;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.lucas.arbackend.repository.security.ApiKeyRepository;
import org.lucas.arbackend.util.CustomUserDetails;
import org.lucas.arbackend.util.TenantContext;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.nio.file.AccessDeniedException;

@Component
@Slf4j
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final ApiKeyRepository apiKeyRepo;
    private final PasswordEncoder encoder;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        log.info("Tenant Filter running for request: {} ", request.getRequestURI());

        String apiKeyHeader = request.getHeader("X-API-KEY");

        try {
            // 1. Check for API KEY (Student / AR App)
            if (apiKeyHeader != null && apiKeyHeader.length() > 12) {
                log.info("X-API-KEY header found: {}", apiKeyHeader);

                String prefix = apiKeyHeader.substring(0 , 12);
                log.info("API Key prefix: {}", prefix);

                // Lookup the API Key in the DB
                ApiKey apiKey = apiKeyRepo.findByPrefix(prefix)
                    .orElseThrow(() -> new BadRequestException("Invalid API Key"));

                // Validate the API Key
                if (!encoder.matches(apiKeyHeader, apiKey.getHashKey())) {
                    throw new AccessDeniedException("Invalid API Key");
                }

                // Set the current tenant for this request
                log.info("Using header to set tenant context to: {}",  apiKey.getOrganisation().getEmail());
                TenantContext.setCurrentTenant(apiKey.getOrganisation().getEmail());

                CustomUserDetails studentPrincipal = new CustomUserDetails("API_KEY_".concat(apiKey.getPrefix()), "", apiKey.getOrgId(), RoleTypes.STUDENT.name());
                // Manually authenticate the Student for this request
                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        studentPrincipal, null, studentPrincipal.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(auth);

            }
            else if (apiKeyHeader != null && apiKeyHeader.length() < 12) {
                throw new BadRequestException("Malformed API Key");
            }

            // 2. Check for Org/Staff Authentication (Already logged in via Basic/JWT)
            else  {

                log.info("Checking Security Context....");
                Authentication auth = SecurityContextHolder.getContext().getAuthentication();

                if (auth != null && auth.isAuthenticated()) {

                log.info("Security context object {}", auth.getDetails());
                log.info("Security context object type {}", auth.getPrincipal());


                if (auth.getPrincipal() instanceof CustomUserDetails user) {
                    log.info("Setting tenant context to: {}", user.getEmail());
                    TenantContext.setCurrentTenant(user.getEmail());
                }
                } else {
                    log.error("No authentication found in TenantFilter for request [{}] for auth [{}]", request.getRequestURI(), auth);
                }
            }
            filterChain.doFilter(request, response);
        } finally {
            // CRITICAL: Always clear the context to prevent memory leaks or tenant bleeding
            TenantContext.clear();
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();

    // Skip docs and health checks
    if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui") || path.startsWith("/api/v1/health")) {
        return true;
    }

    // ONLY skip the Filter for Organisation SIGNUP (POST)
    // All other /organisations/** routes (GET details, PUT profile) NEED the filter to run
    if (path.equals("/api/v1/organisations/signup") && method.equalsIgnoreCase(HttpMethod.POST.name())) {
        return true;
    }

    return path.startsWith("/api/v1/auth");
    }

}
