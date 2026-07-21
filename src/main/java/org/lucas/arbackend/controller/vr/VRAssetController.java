package org.lucas.arbackend.controller.vr;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.vr.asset.VRAssetCreateRequest;
import org.lucas.arbackend.dto.vr.asset.VRAssetResponse;
import org.lucas.arbackend.exception.ErrorDetailsResponse;
import org.lucas.arbackend.service.vr.VRAssetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Tag(name = "11. VR Asset Controller",description = "VR Asset upload and retrieve endpoint")
@RequestMapping("/api/v1/assets")
@RequiredArgsConstructor
public class VRAssetController {
    private final VRAssetService assetService;

    @Operation(summary = "Adding VR Assets",
    description = "Add VR assets to the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asset created"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR')")
    @PostMapping
    public ResponseEntity<VRAssetResponse> registerAsset(@Validated @RequestBody VRAssetCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetService.registerAsset(request));
    }

    @Operation(summary = "Retrieving VR Assets", description = "Retrieve a VR Asset via its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asset served"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found: Asset not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT', 'STUDENT')")
    @GetMapping("/{assetId}")
    public ResponseEntity<VRAssetResponse> getAsset(@PathVariable Long assetId) {
        return ResponseEntity.ok(assetService.getAsset(assetId));
    }
}
