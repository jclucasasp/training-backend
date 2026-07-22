package org.lucas.arbackend.controller.vr;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.vr.scene.*;
import org.lucas.arbackend.exception.ErrorDetailsResponse;
import org.lucas.arbackend.service.vr.VRSceneService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vr/scene")
@RequiredArgsConstructor
@Tag(name = "10. VR Scene", description = "VR Scene API")
public class VRSceneController {
    private final VRSceneService sceneService;

    @Operation(summary = "Active scene and version resolution",
            description = "Load the current active scene and version details for a given chapter section")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scene resolution data served"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found: Section or active scene/version not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT', 'STUDENT')")
    @GetMapping("/active-resolution/{sectionId}")
    public ResponseEntity<VRSceneResolutionResponse> resolveActiveSceneForSection(@PathVariable Long sectionId) {
        return ResponseEntity.ok(sceneService.resolveActiveSceneForSection(sectionId));
    }

    @Operation(summary = "Get all scenes", description = "Get paginated list of all master VR scenes for the organisation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scenes retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT')")
    @GetMapping
    public ResponseEntity<Page<VRSceneResponse>> getAllScenes(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(sceneService.getAllScenes(Pageable.ofSize(size).withPage(page)));
    }

    @Operation(summary = "Create a new scene", description = "Create a new master VR scene")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Scene created successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR')")
    @PostMapping
    public ResponseEntity<VRSceneResponse> createScene(@Validated @RequestBody VRSceneRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sceneService.createScene(request));
    }

    @Operation(summary = "Update a scene", description = "Update an existing master VR scene title or description")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scene updated successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found: Scene not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR')")
    @PutMapping("/{sceneId}")
    public ResponseEntity<VRSceneResponse> updateScene(@PathVariable Long sceneId, @Validated @RequestBody VRSceneRequest request) {
        return ResponseEntity.ok(sceneService.updateScene(sceneId, request));
    }

    @Operation(summary = "Get scene with active hierarchy", description = "Get details of the currently active version for a scene")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scene active hierarchy retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found: Scene or active version not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT')")
    @GetMapping("/{sceneId}")
    public ResponseEntity<VRSceneVersionResponse> getActiveSceneWithHierarchy(@PathVariable Long sceneId) {
        return ResponseEntity.ok(sceneService.getSceneWithActiveHierarchy(sceneId));
    }

    @Operation(summary = "Create a new scene version", description = "Create a new spatial version iteration for a scene")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Scene version created successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found: Scene not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR')")
    @PostMapping("/{sceneId}/versions")
    public ResponseEntity<VRSceneVersionResponse> createSceneVersion(
            @PathVariable Long sceneId,
            @Validated @RequestBody VRSceneVersionRequest request)
    {
        return ResponseEntity.status(HttpStatus.CREATED).body(sceneService.createVersion(sceneId, request));
    }

    @Operation(summary = "Get all scene versions", description = "Get paginated version iterations recorded for a scene")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scene versions retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found: Scene not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT')")
    @GetMapping("/{sceneId}/versions")
    public ResponseEntity<Page<VRSceneVersionResponse>> getSceneVersions(@PathVariable Long sceneId,
                                                                         @RequestParam(defaultValue = "0") int page,
                                                                         @RequestParam(defaultValue = "10") int size)
    {
        return ResponseEntity.ok(sceneService.getAllVersionsForScene(sceneId, PageRequest.of(page, size)));
    }

    @Operation(
    summary = "Activate a scene version",
    description = "Set a current scene version as active. This will automatically deactivate the current active version"
)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Scene version activated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad Request: Version does not belong to the specified scene",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Not Found: Scene or Version not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PutMapping("/{sceneId}/versions/{versionId}/activate")
    public ResponseEntity<VRSceneVersionResponse> activateSceneVersion(
            @PathVariable Long sceneId,
            @PathVariable Long versionId) {
        return ResponseEntity.ok(sceneService.activateVersion(sceneId, versionId));
    }
}
