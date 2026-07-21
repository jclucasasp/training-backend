package org.lucas.arbackend.controller.vr;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.vr.asset.VRAssetCreateRequest;
import org.lucas.arbackend.dto.vr.asset.VRAssetResponse;
import org.lucas.arbackend.exception.ErrorDetailsResponse;
import org.lucas.arbackend.service.vr.VRAssetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
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
    @PostMapping
    public ResponseEntity<VRAssetResponse> registerAsset(@Validated @RequestBody VRAssetCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(assetService.registerAsset(request));
    }

    @Operation(summary = "Retrieving VR Assets",
            description = "Retrieve a VR Assets via its id from the database")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Asset retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
    })
    @GetMapping("/{assetId}")
    public ResponseEntity<VRAssetResponse> getAsset(@PathVariable Long assetId) {
        return ResponseEntity.ok(assetService.getAsset(assetId));
    }
}
