
package org.lucas.arbackend.controller.vr;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.vr.competency.*;
import org.lucas.arbackend.exception.ErrorDetailsResponse;
import org.lucas.arbackend.service.vr.CompetencyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/vr/competencies")
@RequiredArgsConstructor
@Tag(name = "12. VR Competencies", description = "Competency and Skill Tracking API")
public class VRCompetencyController {

    private final CompetencyService competencyService;

    @Operation(summary = "Get all competencies", description = "Get paginated list of defined competencies for the organisation")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Competencies retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT')")
    @GetMapping
    public ResponseEntity<Page<CompetencyResponse>> getAllCompetencies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(competencyService.getAllCompetencies(PageRequest.of(page, size)));
    }

    @Operation(summary = "Get competencies for scene", description = "Retrieve all procedural competencies required for a specific VR scene")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Competencies retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT', 'STUDENT')")
    @GetMapping("/scene/{sceneId}")
    public ResponseEntity<List<CompetencyResponse>> getCompetenciesForScene(@PathVariable Long sceneId) {
        return ResponseEntity.ok(competencyService.getCompetenciesForScene(sceneId));
    }

    @Operation(summary = "Define a competency", description = "Create a new procedural competency definition linked to a VR scene")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Competency created successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR')")
    @PostMapping
    public ResponseEntity<CompetencyResponse> createCompetency(
            @Validated @RequestBody CompetencyCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(competencyService.createCompetency(request));
    }

    @Operation(summary = "Add criteria to competency", description = "Add an observable criterion to a competency definition")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Criterion added successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Competency not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR')")
    @PostMapping("/{competencyId}/criteria")
    public ResponseEntity<CompetencyResponse> addCriterion(
            @PathVariable Long competencyId,
            @Validated @RequestBody CompetencyCriterionRequest request) {
        return ResponseEntity.ok(competencyService.addCriterion(competencyId, request));
    }

    @Operation(summary = "Record a competency assessment", description = "Records assessment output derived from VR session telemetry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Assessment recorded successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Competency or Session not found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT', 'STUDENT')")
    @PostMapping("/{competencyId}/{studentNumber}/assessments")
    public ResponseEntity<CompetencyAssessmentResponse> recordAssessment(
            @PathVariable Long competencyId,
            @PathVariable String studentNumber,
            @Validated @RequestBody CompetencyAssessmentCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(competencyService.recordAssessment(studentNumber, competencyId, request));
    }

    @Operation(summary = "Get student competency dashboard", description = "Retrieves aggregated pass rates and recent assessments for a student")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT', 'STUDENT')")
    @GetMapping("/{studentNumber}")
    public ResponseEntity<StudentCompetencyDashboardResponse> getStudentCompetencyDashboard(
            @PathVariable String studentNumber) {
        return ResponseEntity.ok(competencyService.getStudentCompetencyDashBoard(studentNumber));
    }
}