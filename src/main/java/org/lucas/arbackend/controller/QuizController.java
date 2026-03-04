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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quizzes")
@RequiredArgsConstructor
@Tag(name = "Quiz Management", description = "Endpoints for creating, fetching, and submitting course quizzes")
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
}
