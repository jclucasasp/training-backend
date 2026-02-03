package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.lucas.arbackend.dto.security.ApiKeyResponse;
import org.lucas.arbackend.dto.organisation.OrgSignupRequest;
import org.lucas.arbackend.dto.organisation.OrganisationResponse;
import org.lucas.arbackend.dto.organisation.ProfileRequest;
import org.lucas.arbackend.service.ApiKeyService;
import org.lucas.arbackend.service.OrganisationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("api/v1/organisations")
public class OrganisationController {
    // TODO: When using JWT use claims to get the current tenant ID from the TenetContext class
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
    @GetMapping("/{orgId}")
    public ResponseEntity<OrganisationResponse> getDetails(
            @Parameter(description = "The unique ID of the organisation", example = "1")
            @PathVariable Long orgId) {
        return ResponseEntity.ok(orgService.getOrganisationDetails(orgId));
    }

    @Operation(summary = "Update Profile",
               description = "Updates the business registration number, VAT number, and display name.")
    @PutMapping("/{orgId}/profile")
    public ResponseEntity<Void> updateProfile(@PathVariable Long orgId, @Valid @RequestBody ProfileRequest request) {
        orgService.updateProfile(orgId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Generate API Key",
               description = "Generates a new secure API key. The raw key is returned ONLY ONCE for security.")
    @PostMapping("/{orgId}/api-keys")
    public ResponseEntity<ApiKeyResponse> createApiKey(@PathVariable Long orgId) {
        return ResponseEntity.ok(apiKeyService.generateKeyForOrg(orgId));
    }
}
