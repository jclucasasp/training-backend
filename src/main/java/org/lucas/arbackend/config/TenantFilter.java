package org.lucas.arbackend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.service.JwtService;
import org.lucas.arbackend.util.TenantContext;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class TenantFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // Extract the custom 'orgId' claim we added to the JWT earlier
            Long orgId = jwtService.extractClaim(token, claims -> claims.get("orgId", Long.class));

            if (orgId != null) {
                TenantContext.setCurrentTenant(orgId);
            }
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // CRITICAL: Always clear the context after the request is done
            // to prevent memory leaks or data bleeding between threads
            TenantContext.clear();
        }
    }
}
