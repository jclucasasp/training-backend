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
import org.lucas.arbackend.exception.ErrorDetailsResponse; // Ensure this is imported
import org.lucas.arbackend.service.organisation.OrganisationService;
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
    @PostMapping("/signup")
    @JsonView(AccessLevelViews.Public.class)
    public ResponseEntity<OrganisationResponse> signup(@Validated(ValidatedLabel.OnCreate.class) @RequestBody OrganisationRequest request) {
        return ResponseEntity.ok(orgService.signup(request));
    }

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
    @GetMapping("/details")
    @JsonView(AccessLevelViews.Public.class)
    public ResponseEntity<OrganisationResponse> getDetails() {
        return ResponseEntity.ok(orgService.getOrganisationDetails());
    }

    @Operation(summary = "Update Profile", description = "Updates the business registration number, VAT number, and display name.")
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
    @PutMapping("/update")
    public ResponseEntity<OrganisationResponse> updateProfile(@Validated(ValidatedLabel.OnUpdate.class) @RequestBody OrganisationRequest request) {
        OrganisationResponse response = orgService.updateProfile(request);
        return ResponseEntity.ok(response);
    }

//    @Operation(summary = "Generate API Key", learningObjectives = "Generates a new secure API key. The raw key is returned ONLY ONCE for security.")
//    @ApiResponses(value = {
//            @ApiResponse(responseCode = "200", learningObjectives = "API Key generated successfully"),
//            @ApiResponse(responseCode = "403", learningObjectives = "Access Denied: You do not have permission to generate keys",
//                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
//            @ApiResponse(responseCode = "401", learningObjectives = "Unauthorized",
//                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
//            @ApiResponse(responseCode = "500", learningObjectives = "Internal server error",
//                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
//    })
//    @PostMapping("/api-keys")
//    public ResponseEntity<ApiKeyResponse> createApiKey() {
//        return ResponseEntity.ok(apiKeyService.generateKeyForOrg(orgId));
//    }
}