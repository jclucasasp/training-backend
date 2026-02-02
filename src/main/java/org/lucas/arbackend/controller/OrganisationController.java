package org.lucas.arbackend.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.lucas.arbackend.dto.organisation.ApiKeyResponse;
import org.lucas.arbackend.dto.organisation.OrgSignupRequest;
import org.lucas.arbackend.dto.organisation.OrganisationResponse;
import org.lucas.arbackend.dto.organisation.ProfileRequest;
import org.lucas.arbackend.service.OrganisationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("api/v1/organisations")
public class OrganisationController {
    // TODO: Implement controller logic
    private final OrganisationService orgService;

    // PUBLIC: Any visitor can sign up
    @PostMapping("/signup")
    public ResponseEntity<OrganisationResponse> signUp(@RequestBody OrgSignupRequest request) {
        return ResponseEntity.ok(orgService.signUp(request));
    }

    // PROTECTED: Only the logged-in Org (or their Staff) can see/edit this
    @GetMapping("/me")
    public ResponseEntity<OrganisationResponse> getMyProfile() {
//        Only use this when using Jwt tokens with the tenentId in it and set in the TenentContext
//        Long currentOrgId = TenantContext.getTenantId();
//        return ResponseEntity.ok(orgService.getOrganisationDetails(currentOrgId));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/me/profile")
    public ResponseEntity<Void> updateProfile(@RequestBody ProfileRequest request) {
//        Long currentOrgId = TenantContext.getTenantId();
//        orgService.updateProfile(currentOrgId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api-keys")
    public ResponseEntity<ApiKeyResponse> createApiKey(@RequestBody Map<String, String> body) {
//        Long currentOrgId = TenantContext.getTenantId();
//        return ResponseEntity.ok(orgService.generateApiKey(currentOrgId));
        return ResponseEntity.ok().build();
    }
}
