package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.lucas.arbackend.util.ValidatedLabel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/student")
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
    @PostMapping("/enroll")
    public ResponseEntity<EnrollmentResponse> enroll(@Validated(ValidatedLabel.OnCreate.class) @RequestBody StudentRequest request) {
        return ResponseEntity.ok(studentService.enrollStudent(request));
    }

    @Operation(
            summary = "Register Student for Quiz",
            description = "Links a student to a specific quiz. Both must belong to the same organisation as the authenticated user."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Student successfully registered for the quiz"),
            @ApiResponse(responseCode = "400", description = "Invalid student number or quiz ID provided",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Cross-tenant access denied",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Student or Quiz not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAuthority('ORG_ADMIN') or hasAuthority('COURSE_EDITOR') or hasAuthority('STUDENT')")
    @PostMapping("/{studentNumber}/quiz/{quizId}/register")
    public ResponseEntity<Void> registerForQuiz(
            @Parameter(description = "The unique student number", example = "STU-12345")
            @PathVariable String studentNumber,
            @Parameter(description = "The ID of the quiz", example = "1")
            @PathVariable Long quizId) {

        studentService.registerStudentForQuiz(studentNumber, quizId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get Student List", description = "Paginated list of all students registered under this tenant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Organisation not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    @PreAuthorize("hasAuthority('ORG_ADMIN') or hasAuthority('COURSE_EDITOR') or hasAuthority('SUPPORT')")
    @GetMapping("/list")
    public ResponseEntity<Page<StudentResponse>> listStudents(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size,
                                                           @RequestParam(defaultValue = "id") String sort) {
        return ResponseEntity.ok(studentService.getPaginatedStudents(PageRequest.of(page, size, Sort.by(sort))));
    }
}