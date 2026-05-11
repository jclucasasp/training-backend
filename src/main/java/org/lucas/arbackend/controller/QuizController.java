package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.quiz.*;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.repository.organisation.StaffRepository;
import org.lucas.arbackend.repository.student.StudentRepository;
import org.lucas.arbackend.service.quiz.QuizService;
import org.lucas.arbackend.util.CustomUserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/chapterQuizzes")
@RequiredArgsConstructor
@Tag(name = "6. Quiz Management", description = "Endpoints for creating, fetching, and submitting course chapterQuizzes")
public class QuizController {

    private final QuizService quizService;
    private final StaffRepository staffRepo; // Required to resolve the 'Staff' entity from the principal
    private final StudentRepository studentRepo; // Required for submission logic

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR')")
    @Operation(summary = "Create a new quiz", description = "Allows a staff member to create a quiz for a specific course.")
    public ResponseEntity<QuizResponse> createQuiz(
            @Valid @RequestBody QuizRequest request,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // Resolve the Staff entity from the database using the logged-in user's ID
        Staff creator = staffRepo.findById(userDetails.getId())
                .orElseThrow(() -> new EntityNotFoundException("Staff member not found"));

        return new ResponseEntity<>(quizService.createQuiz(request, creator), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT', 'STUDENT')")
    @Operation(summary = "Get quiz details", description = "Fetches quiz questions and metadata by ID.")
    public ResponseEntity<QuizResponse> getQuizById(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuizById(id));
    }

    @PostMapping("/{id}/submit")
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'SUPPORT', 'STUDENT')")
    @Operation(summary = "Submit a quiz attempt", description = "Calculates the score and records a student's attempt.")
    public ResponseEntity<QuizResultResponse> submitAttempt(
            @PathVariable Long id,
            @Valid @RequestBody QuizSubmissionRequest submission,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        // We pass the email/studentNumber to the service as per our previous service fix
        return ResponseEntity.ok(quizService.submitAndGradeQuiz(userDetails.getUsername(), id, submission));
    }

    @PutMapping("/{id}/course/{courseId}/assign-chapter/{chapterId}")
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR')")
    @Operation(summary = "Assign quiz to a chapter")
    public ResponseEntity<Void> assignToChapter(
            @PathVariable Long id,
            @PathVariable Long courseId,
            @PathVariable Long chapterId
    ) {
        quizService.assignQuizToChapter(id, courseId, chapterId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/assign-enrolled-students/course/{courseId}")
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR')")
    @Operation(summary = "Assign quiz to all enrolled students")
    public ResponseEntity<Void> assignToEnrolledStudents(
            @PathVariable Long id,
            @PathVariable Long courseId
    ) {
        quizService.assignQuizToEnrolledStudents(id, courseId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ==========================================
    // QUIZ METADATA & STRUCTURE MANAGEMENT
    // ==========================================

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR')")
    @Operation(summary = "Update quiz metadata")
    public ResponseEntity<QuizResponse> updateQuizMetadata(
            @PathVariable Long id,
            @Valid @RequestBody QuizRequest request) {
        return ResponseEntity.ok(quizService.updateQuizMetadata(id, request));
    }

    // ==========================================
    // QUESTION MANAGEMENT
    // ==========================================

    @PostMapping("/{id}/questions")
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR', 'STUDENT')")
    @Operation(summary = "Add a question to an existing quiz")
    public ResponseEntity<Void> addQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequest request) {
        quizService.addQuestionToQuiz(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{id}/questions/{questionId}")
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR')")
    @Operation(summary = "Update an existing question")
    public ResponseEntity<Void> updateQuestion(
            @PathVariable Long id,
            @PathVariable Long questionId,
            @Valid @RequestBody QuestionRequest request) {
        quizService.updateQuestion(id, questionId, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}/questions/{questionId}")
    @PreAuthorize("hasAnyAuthority('ORG_ADMIN', 'COURSE_EDITOR')")
    @Operation(summary = "Remove a question from a quiz")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long id,
            @PathVariable Long questionId) {
        quizService.removeQuestion(id, questionId);
        return ResponseEntity.noContent().build();
    }
}