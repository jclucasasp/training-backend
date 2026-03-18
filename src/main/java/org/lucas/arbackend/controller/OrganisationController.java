package org.lucas.arbackend.controller;

import com.fasterxml.jackson.annotation.JsonView;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import org.lucas.arbackend.dto.organisation.OrganisationRequest;
import org.lucas.arbackend.dto.organisation.OrganisationResponse;
import org.lucas.arbackend.dto.security.ApiKeyResponse;
import org.lucas.arbackend.entity.security.ApiKey;
import org.lucas.arbackend.exception.ErrorDetailsResponse; // Ensure this is imported
import org.lucas.arbackend.service.organisation.OrganisationService;
import org.lucas.arbackend.service.security.ApiKeyService;
import org.lucas.arbackend.util.AccessLevelViews;
import org.lucas.arbackend.util.ValidatedLabel;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("api/v1/organisation")
@Tag(name = "2. Organisations", description = "Create, update, and retrieve organisation details.")
public class OrganisationController {

    private final OrganisationService orgService;
    private final ApiKeyService apiKeyService;

    @PostMapping("/signup")
    @Operation(summary = "Create a new organisation", description = "Performs an atomic signup: Creates the Org, Profile, and initial Subscription Plan.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Organisation created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed (Invalid input data)",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "409", description = "Conflict: Email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error during processing",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @JsonView(AccessLevelViews.Public.class)
    public ResponseEntity<OrganisationResponse> signup(@Validated(ValidatedLabel.OnCreate.class) @RequestBody OrganisationRequest request) {
        return ResponseEntity.ok(orgService.signup(request));
    }

    @GetMapping("/details")
    @Operation(summary = "Get Organisation Details", description = "Retrieves full profile and subscription status using the Org ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Full authentication is required to access this resource",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Organisation not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @JsonView(AccessLevelViews.Public.class)
    public ResponseEntity<OrganisationResponse> getDetails() {
        return ResponseEntity.ok(orgService.getOrganisationDetails());
    }

    @PutMapping("/update")
    @Operation(summary = "Update Profile", description = "Updates the business registration number, VAT number, and display fileName.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation error in the request body",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Organisation or Profile not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    public ResponseEntity<OrganisationResponse> updateProfile(@Validated(ValidatedLabel.OnUpdate.class) @RequestBody OrganisationRequest request) {
        OrganisationResponse response = orgService.updateProfile(request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/api-key/revoke")
    @Operation(summary = "Revoke API Key", description = "Revokes the API Key for the current organisation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API Key revoked successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Organisation not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
            content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))  })
    public ResponseEntity<Void> revokeApiKey() {
        apiKeyService.revokeApiKeyForOrg();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/api-key/generate")
    @Operation(summary = "Generate API Key", description = "Generates a new API Key for the current organisation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API Key generated successfully"),
            @ApiResponse(responseCode = "400", description = "Organisation has no active subscription or API Key already exists",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Organisation not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    public ResponseEntity<ApiKeyResponse> renewApiKey() {
        return ResponseEntity.ok(apiKeyService.generateKeyForOrg(new ApiKey(), false));
    }

}