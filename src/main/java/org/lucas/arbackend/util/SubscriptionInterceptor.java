//package org.lucas.arbackend.util;
//
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import lombok.RequiredArgsConstructor;
//import org.jspecify.annotations.NonNull;
//import org.lucas.arbackend.repository.organisation.OrganisationSubscriptionRepository;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//
//@Component
//@RequiredArgsConstructor
//public class SubscriptionInterceptor implements HandlerInterceptor {
//
//    private final OrganisationSubscriptionRepository subRepo;
//
//     @Override
//    public boolean preHandle(HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
//        String path = request.getRequestURI();
//
//        // 1. Skip check for public routes (Docs, Signup, etc.)
//        if (path.contains("/v3/api-docs") || path.contains("/swagger-ui") || path.contains("/organisations")) {
//            return true;
//        }
//
//        Long orgId = TenantContext.getCurrentTenant();
//        if (orgId != null) {
//            // This call is now CACHED in Redis via the Repository
//            boolean isActive = subRepo.findActiveByOrganisationId(orgId).isPresent();
//
//            if (!isActive) {
//                response.setStatus(402); // Payment Required
//                response.getWriter().write("Subscription expired or inactive. Please contact billing.");
//                return false;
//            }
//        }
//
//        return true;
//    }
//}
