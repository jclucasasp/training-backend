package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.quiz.QuizAttemptResponse;
import org.lucas.arbackend.dto.quiz.QuizSubmissionRequest;
import org.lucas.arbackend.dto.student.EnrollmentResponse;
import org.lucas.arbackend.dto.student.StudentRequest;
import org.lucas.arbackend.dto.student.StudentResponse;
import org.lucas.arbackend.exception.ErrorDetailsResponse;
import org.lucas.arbackend.service.student.StudentService;
import org.lucas.arbackend.util.ValidatedLabel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

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
//    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR')")
    @PostMapping("/{studentNumber}/quiz/{quizId}/register")
    public ResponseEntity<Void> registerForQuiz(
            @Parameter(description = "The unique student number", example = "STU-12345")
            @PathVariable String studentNumber,
            @Parameter(description = "The ID of the quiz", example = "1")
            @PathVariable Long quizId) {

        studentService.registerStudentForQuiz(studentNumber, quizId);
        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "Update student course progress",
            description = "Updates the progress percentage for a specific section and recalculates the total course progress. Accessible by Students, Admins, and Support."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Progress updated successfully"),
            @ApiResponse(responseCode = "404", description = "Student or Section not found"),
            @ApiResponse(responseCode = "403", description = "Access denied")
    })
//    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'STUDENT')")
    @PatchMapping("/{studentNumber}/progress")
    public ResponseEntity<Void> updateProgress(
            @Parameter(description = "The student's unique number") @PathVariable String studentNumber,
            @Parameter(description = "The ID of the chapter section") @RequestParam Long sectionId,
            @Parameter(description = "Progress percentage (0.0 to 100.0)") @RequestParam Double percentage) {
        studentService.updateProgress(studentNumber, sectionId, percentage);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get student learning dashboard",
            description = "Retrieves a list of all course enrollments and current progress for a specific student."
    )
    @GetMapping("/{studentNumber}/dashboard")
//    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'STUDENT')")
    public ResponseEntity<List<EnrollmentResponse>> getDashboard(
            @Parameter(description = "The student's unique number") @PathVariable String studentNumber) {
        return ResponseEntity.ok(studentService.getStudentDashboard(studentNumber));
    }

    @Operation(
            summary = "Resume course from last viewed section",
            description = "Returns enrollment details including the lastSectionId for a specific course to allow the student to resume where they left off."
    )
    @GetMapping("/{studentNumber}/resume/{courseSlug}")
//    @PreAuthorize("hasAuthority('STUDENT')")
    public ResponseEntity<EnrollmentResponse> resumeCourse(
            @Parameter(description = "The student's unique number") @PathVariable String studentNumber,
            @Parameter(description = "The URL slug of the course") @PathVariable String courseSlug) {
        return ResponseEntity.ok(studentService.getResumeDetails(studentNumber, courseSlug));
    }


 @Operation(
        summary = "Get quiz attempt history",
        description = "Retrieves a list of all previous attempts made by a specific student for a specific quiz. Useful for showing a history table."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved attempt history"),
        @ApiResponse(responseCode = "404", description = "Student or Quiz not found")
    })
//    @PreAuthorize("hasAuthority('STUDENT')")
    @GetMapping("/{studentNumber}/chapterQuizzes/{quizId}/attempts")
    public ResponseEntity<List<QuizAttemptResponse>> getQuizAttempts(
            @Parameter(description = "Unique student identifier") @PathVariable String studentNumber,
            @Parameter(description = "ID of the quiz") @PathVariable Long quizId) {
        return ResponseEntity.ok(studentService.getQuizAttempts(studentNumber, quizId));
    }

    @Operation(
        summary = "Get specific attempt details",
        description = "Retrieves the full details of a single attempt, including the calculated score and the submitted answers (JSON)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved attempt details"),
        @ApiResponse(responseCode = "403", description = "Access denied - Attempt belongs to another organization"),
        @ApiResponse(responseCode = "404", description = "Attempt ID not found")
    })
//    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'STUDENT')")
    @GetMapping("/attempts/{attemptId}")
    public ResponseEntity<QuizAttemptResponse> getAttemptDetail(
            @Parameter(description = "The unique ID of the quiz attempt") @PathVariable Long attemptId) {
        return ResponseEntity.ok(studentService.getAttemptDetails(attemptId));
    }

    @Operation(
            summary = "Review a specific quiz attempt",
            description = "Returns the raw JSON of submitted answers for a specific attempt. Requires tenant-level access."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Attempt found and returned",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "403", description = "Access denied - Attempt belongs to another organization"),
            @ApiResponse(responseCode = "404", description = "Attempt not found")
    })
//    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'STUDENT')")
    @GetMapping("/{studentNumber}/attempts/{attemptId}/review")
    public ResponseEntity<String> getAttemptReview(
            @Parameter(description = "The unique student number") @PathVariable String studentNumber,
            @Parameter(description = "The ID of the specific quiz attempt") @PathVariable Long attemptId) {

        String jsonReview = studentService.getAttemptReview(studentNumber, attemptId);

        // We return it as a String, but tell the browser/Postman it is JSON
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(jsonReview);
    }

    @Operation(summary = "Get Student List", description = "Paginated list of all students registered under this tenant.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Organisation not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    //    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'STUDENT')")
    @GetMapping("/list")
    public ResponseEntity<Page<StudentResponse>> listStudents(@RequestParam(defaultValue = "0") int page,
                                                           @RequestParam(defaultValue = "10") int size,
                                                           @RequestParam(defaultValue = "id") String sort) {
        return ResponseEntity.ok(studentService.getPaginatedStudents(PageRequest.of(page, size, Sort.by(sort))));
    }
}