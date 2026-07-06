package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.lucas.arbackend.dto.vr.*;
import org.lucas.arbackend.exception.ErrorDetailsResponse;
import org.lucas.arbackend.service.vr.VRSessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/vr")
@RequiredArgsConstructor
@Tag(name = "9. VR Training", description = "VR session telemetry and replay data")
public class VRTrainingController {
    private final VRSessionService sessionService;

    @Operation(summary = "Start VR Session", description = "Initiates a new VR training session for the authenticated student.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Session started",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VRSessionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Section not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAuthority('STUDENT')")
    @PostMapping("/sessions")
    public ResponseEntity<VRSessionResponse> startSession(
            @Validated @RequestBody VRSessionStartRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sessionService.startSession(request));
    }

    @Operation(summary = "End VR Session", description = "Finalizes a VR session with telemetry summary.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Session ended successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Session not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "409", description = "Session already ended",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAuthority('STUDENT')")
    @PatchMapping("/sessions/{sessionId}")
    public ResponseEntity<Void> endSession(
            @PathVariable Long sessionId,
            @Validated @RequestBody VRSessionEndRequest request
            ) {
        sessionService.endSession(sessionId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get Session Details", description = "Retrieves a specific VR session summary.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Session found",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VRSessionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Session not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT', 'STUDENT')")
    @GetMapping("/sessions/{sessionId}")
    public ResponseEntity<VRSessionResponse> getSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(sessionService.getSession(sessionId));
    }

    @Operation(summary = "Record VR Events", description = "Batch records telemetry events from the VR headset.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Events recorded"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Session not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "409", description = "Session already ended",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAuthority('STUDENT')")
    @PostMapping("/sessions/{sessionId}/events")
    public ResponseEntity<Void> recordEvents(
            @PathVariable Long sessionId,
            @Validated @RequestBody List<VREventRequest> events
    ) {
        sessionService.batchRecordingEvents(sessionId, events);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Get Session Events", description = "Paginated telemetry events for a session.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Events retrieved",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VREventResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Session not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT', 'STUDENT')")
    @GetMapping("/sessions/{sessionId}/events")
     public ResponseEntity<Page<VREventResponse>> getSessionEvents(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "100") int size) {
        return ResponseEntity.ok(sessionService.getSessionEvents(sessionId, PageRequest.of(page, size)));
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sessions retrieved",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VRSessionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT', 'STUDENT')")
    @GetMapping("/student/{studentNumber}/sessions")
    public ResponseEntity<Page<VRSessionResponse>> getStudentSessions(
            @PathVariable String studentNumber,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(sessionService.getStudentSessions(studentNumber, PageRequest.of(page, size)));
    }

    @Operation(summary = "Get All VR Sessions", description = "Paginated list of all VR sessions for the organisation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sessions retrieved",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VRSessionResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT')")
    @GetMapping("/sessions")
    public ResponseEntity<Page<VRSessionResponse>> getAllSessions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(sessionService.getAllSessions(PageRequest.of(page, size)));
    }

    @Operation(summary = "Get Student VR Analytics", description = "Aggregated analytics for a student's VR training performance.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Analytics retrieved",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = VRStudentAnalyticsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT', 'STUDENT')")
    @GetMapping("/student/{studentNumber}/analytics")
    public ResponseEntity<VRStudentAnalyticsResponse> getStudentAnalytics(
            @PathVariable String studentNumber) {
        return ResponseEntity.ok(sessionService.getStudentAnalytics(studentNumber));
    }

}
