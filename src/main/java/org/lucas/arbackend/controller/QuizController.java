package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.quiz.*;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.service.quiz.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@Tag(name = "6. Quiz Management", description = "Endpoints for creating, fetching, and submitting course quizzes")
public class QuizController {
     private final QuizService quizService;

    @PostMapping
    @Operation(summary = "Create a new quiz", description = "Allows a staff member to create a quiz for a specific course.")
    public ResponseEntity<QuizResponse> createQuiz(
            @RequestBody QuizRequest request,
            // In a real app, this 'creator' is resolved from the security context/JWT
            @Parameter(hidden = true) Staff creator
    ) {
        return new ResponseEntity<>(quizService.createQuiz(request, creator), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get quiz details", description = "Fetches quiz questions and metadata by ID.")
    public ResponseEntity<QuizResponse> getQuizById(@PathVariable Long id) {
        return ResponseEntity.ok(quizService.getQuizById(id));
    }

    @PostMapping("/{id}/submit")
    @Operation(summary = "Submit a quiz attempt", description = "Calculates the score and records a student's attempt.")
    public ResponseEntity<QuizResultResponse> submitAttempt(
            @PathVariable Long id,
            @RequestBody QuizSubmission submission,
            // Resolved from current session/JWT
            @Parameter(hidden = true) Student student
    ) {
        return ResponseEntity.ok(quizService.submitAttempt(id, student, submission));
    }

     @PutMapping("/{id}/assign-chapter/{chapterId}")
    @Operation(
        summary = "Assign quiz to a chapter",
        description = "Creates a relationship between an existing quiz and a course chapter."
    )
    public ResponseEntity<Void> assignToChapter(
            @PathVariable @Parameter(description = "The ID of the Quiz") Long id,
            @PathVariable @Parameter(description = "The ID of the Chapter") Long chapterId
    ) {
        quizService.assignQuizToChapter(id, chapterId);
        return ResponseEntity.noContent().build();
    }

     @PostMapping("/{id}/assign-enrolled-students/course/{courseId}")
    @Operation(
        summary = "Assign quiz to all enrolled students",
        description = "Identifies all students enrolled in the specified course and creates a StudentQuiz record for each."
    )
    public ResponseEntity<Void> assignToEnrolledStudents(
            @PathVariable @Parameter(description = "The ID of the Quiz") Long id,
            @PathVariable @Parameter(description = "The ID of the Course") Long courseId
    ) {
        quizService.assignQuizToEnrolledStudents(id, courseId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ==========================================
    // QUIZ METADATA & STRUCTURE MANAGEMENT
    // ==========================================

    @Operation(summary = "Update quiz metadata", description = "Updates the name, passing score, or other top-level attributes of a quiz.")
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ORG_ADMIN') or hasAuthority('COURSE_EDITOR')")
    public ResponseEntity<QuizResponse> updateQuizMetadata(
            @PathVariable Long id,
            @RequestBody QuizRequest request) {
        return ResponseEntity.ok(quizService.updateQuizMetadata(id, request));
    }

    // ==========================================
    // QUESTION MANAGEMENT (GRANULAR)
    // ==========================================

    @Operation(summary = "Add a question to an existing quiz",
            description = "Creates a new question with its options and links it to the specified quiz.")
    @PostMapping("/{id}/questions")
    @PreAuthorize("hasAuthority('ORG_ADMIN') or hasAuthority('COURSE_EDITOR')")
    public ResponseEntity<Void> addQuestion(
            @PathVariable Long id,
            @RequestBody QuestionRequest request) {
        quizService.addQuestionToQuiz(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "Update an existing question",
            description = "Updates the text of a question and replaces its options with the new list provided.")
    @PutMapping("/{id}/questions/{questionId}")
    @PreAuthorize("hasAuthority('ORG_ADMIN') or hasAuthority('COURSE_EDITOR')")
    public ResponseEntity<Void> updateQuestion(
            @PathVariable Long id,
            @PathVariable Long questionId,
            @RequestBody QuestionRequest request) {
        quizService.updateQuestion(id, questionId, request);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Remove a question from a quiz",
            description = "Deletes the question and all its associated options from the quiz.")
    @DeleteMapping("/{id}/questions/{questionId}")
    @PreAuthorize("hasAuthority('ORG_ADMIN') or hasAuthority('COURSE_EDITOR')")
    public ResponseEntity<Void> deleteQuestion(
            @PathVariable Long id,
            @PathVariable Long questionId) {
        quizService.removeQuestion(id, questionId);
        return ResponseEntity.noContent().build();
    }

}
