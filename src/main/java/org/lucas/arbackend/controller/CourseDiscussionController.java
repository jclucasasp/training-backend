package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.QAndA.*;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.service.QAndA.CourseDiscussionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/discussions")
@RequiredArgsConstructor
@Tag(name = "Course Discussions", description = "Endpoints for course-specific Q&A between students and staff")
public class CourseDiscussionController {
     private final CourseDiscussionService discussionService;

    @GetMapping("/course/{courseId}")
    @Operation(summary = "Get all questions for a course", description = "Returns a list of questions filtered by course and organisation.")
    public ResponseEntity<List<CourseQuestionResponse>> getQuestions(@PathVariable Long courseId) {
        return ResponseEntity.ok(discussionService.getQuestionsByCourse(courseId));
    }

    @PostMapping("/questions")
    @Operation(summary = "Ask a question", description = "Allows a student to post a new question to a course section.")
    public ResponseEntity<CourseQuestionResponse> askQuestion(
            @RequestBody CourseQuestionRequest request,
            @RequestAttribute Student currentStudent // Simplified representation of auth context
    ) {
        return new ResponseEntity<>(discussionService.askQuestion(request, currentStudent), HttpStatus.CREATED);
    }

    @PostMapping("/questions/{questionId}/replies")
    @Operation(summary = "Post a reply", description = "Allows students or staff to reply to a specific question.")
    public ResponseEntity<ReplyResponse> postReply(
            @PathVariable Long questionId,
            @RequestBody ReplyRequest request,
            @RequestAttribute(required = false) Student student,
            @RequestAttribute(required = false) Staff staff
    ) {
        // Logic inside the service handles if student or staff is replying
        return new ResponseEntity<>(discussionService.postReply(questionId, request, student, staff), HttpStatus.CREATED);
    }
}
