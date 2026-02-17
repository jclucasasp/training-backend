package org.lucas.arbackend.config.filter;

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

    private final ApiKeyRepository apiKeyRepo; // Ideally, move this logic to a cached Service
    private final PasswordEncoder encoder;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String apiKeyHeader = request.getHeader("X-API-KEY");

        try {
            if (apiKeyHeader != null) {
                // PATH A: Student via API Key
                handleApiKeyAuthentication(apiKeyHeader);
            } else {
                // PATH B: Staff/Org via Session (Already populated by Spring Session)
                handleSessionAuthentication();
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear();
        }
    }

    private void handleApiKeyAuthentication(String apiKeyHeader) throws BadRequestException, AccessDeniedException {
        if (apiKeyHeader.length() < 12) throw new BadRequestException("Malformed API Key");

        String prefix = apiKeyHeader.substring(0, 12);

        // This lookup should be cached in Redis for better performance!
        ApiKey apiKey = apiKeyRepo.findByPrefix(prefix)
                .orElseThrow(() -> new BadRequestException("Invalid API Key"));

        if (!encoder.matches(apiKeyHeader, apiKey.getHashKey())) {
            throw new AccessDeniedException("Invalid API Key");
        }

        Long orgId = apiKey.getOrganisation().getId();
        TenantContext.setCurrentTenant(orgId);

        // Manually set Student in SecurityContext so @PreAuthorize works
        CustomUserDetails student = new CustomUserDetails("API_KEY_" + prefix, "", orgId, RoleTypes.STUDENT.name());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(student, null, student.getAuthorities())
        );
    }

    private void handleSessionAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.getPrincipal() instanceof CustomUserDetails user) {
            // Simply pull the ID from the already-loaded session in Redis
            TenantContext.setCurrentTenant(user.getOrgId());
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/v3/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/api/v1/auth") ||
                path.equals("/api/v1/organisations/signup");
    }
}

