package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.student.EnrollmentResponse;
import org.lucas.arbackend.dto.student.StudentRequest;
import org.lucas.arbackend.dto.student.StudentResponse;
import org.lucas.arbackend.exception.ErrorDetailsResponse;
import org.lucas.arbackend.service.student.StudentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/students")
@Tag(name = "5. Student Management", description = "Student onboarding and enrollment tracking")
public class StudentController {

    private final StudentService studentService;

    @Operation(summary = "Enroll Student in Course",
            description = "Verifies if the student exists in the Org; if not, creates them and starts enrollment.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student enrolled successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data or validation failure",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Organisation or Course not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PostMapping("/org/{orgId}/enroll")
    public ResponseEntity<EnrollmentResponse> enroll(@PathVariable Long orgId, @RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentService.enrollStudent(orgId, request));
    }

    @Operation(summary = "Get Student List", description = "Paginated list of all students registered under this tenant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Organisation not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @GetMapping("/org/{orgId}")
    public ResponseEntity<Page<StudentResponse>> listStudents(@PathVariable Long orgId, Pageable pageable) {
        return ResponseEntity.ok(studentService.getPaginatedStudents(orgId, pageable));
    }
}