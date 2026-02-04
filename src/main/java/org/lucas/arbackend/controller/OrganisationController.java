package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.lucas.arbackend.dto.security.ApiKeyResponse;
import org.lucas.arbackend.dto.organisation.OrgSignupRequest;
import org.lucas.arbackend.dto.organisation.OrganisationResponse;
import org.lucas.arbackend.dto.organisation.ProfileRequest;
import org.lucas.arbackend.service.ApiKeyService;
import org.lucas.arbackend.service.OrganisationService;
import org.lucas.arbackend.util.TenantContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("api/v1/organisations")
@Tag(name = "1. Organisations", description = "Create, update, and retrieve organisation details.")
public class OrganisationController {
    private final OrganisationService orgService;
    private final ApiKeyService apiKeyService;

    @Operation(summary = "Create a new organisation", description = "Performs an atomic signup: Creates the Org, Profile, and initial Subscription Plan.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Organisation created successfully",
            content = @Content(schema = @Schema(implementation = OrganisationResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or validation error"),
        @ApiResponse(responseCode = "409", description = "Email address already registered")
    })
    @PostMapping("/signup")
    public ResponseEntity<OrganisationResponse> signUp(@Valid @RequestBody OrgSignupRequest request) {
        return ResponseEntity.ok(orgService.signUp(request));
    }

    @Operation(summary = "Get Organisation Details",
               description = "Retrieves the business profile and active subscription status using the Org ID.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Details retrieved successfully"),
        @ApiResponse(responseCode = "404", description = "Organisation not found")
    })
    @GetMapping("/details")
    public ResponseEntity<OrganisationResponse> getDetails() {
        return ResponseEntity.ok(orgService.getOrganisationDetails());
    }

    @Operation(summary = "Update Profile",
               description = "Updates the business registration number, VAT number, and display name.")
    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(@Valid @RequestBody ProfileRequest request) {
        Long orgId = TenantContext.getCurrentTenant();
        orgService.updateProfile(orgId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Generate API Key",
               description = "Generates a new secure API key. The raw key is returned ONLY ONCE for security.")
    @PostMapping("/api-keys")
    public ResponseEntity<ApiKeyResponse> createApiKey() {
        Long orgId = TenantContext.getCurrentTenant();
        return ResponseEntity.ok(apiKeyService.generateKeyForOrg(orgId));
    }
}
