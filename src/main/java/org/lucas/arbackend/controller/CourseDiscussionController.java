package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.QAndA.*;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.exception.ErrorDetailsResponse;
import org.lucas.arbackend.service.QAndA.CourseDiscussionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/discussions")
@RequiredArgsConstructor
@Tag(name = "5. Course Discussions", description = "Endpoints for course-specific Q&A between students and staff")
public class CourseDiscussionController {
     private final CourseDiscussionService discussionService;

    @GetMapping("/course/{courseId}")
       @Operation(summary = "Get all questions for a course", description = "Returns a list of questions filtered by course and organisation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Questions retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Course not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    public ResponseEntity<List<CourseQuestionResponse>> getQuestions(@PathVariable Long courseId) {
        return ResponseEntity.ok(discussionService.getQuestionsByCourse(courseId));
    }

    @PostMapping("/questions")
    @Operation(summary = "Ask a question", description = "Allows a student to post a new question to a course section.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Question created"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Only students can ask questions",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    public ResponseEntity<CourseQuestionResponse> askQuestion(
            @RequestBody CourseQuestionRequest request,
            @RequestAttribute Student currentStudent // Simplified representation of auth context
    ) {
        return new ResponseEntity<>(discussionService.askQuestion(request, currentStudent), HttpStatus.CREATED);
    }

    @PostMapping("/questions/{questionId}/replies")
    @Operation(summary = "Post a reply", description = "Allows students or staff to reply to a specific question. Only staff can accept an answer.")
     @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Reply posted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Question not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    public ResponseEntity<ReplyResponse> postReply(
            @PathVariable Long questionId,
            @RequestBody ReplyRequest request,
            @RequestAttribute(required = false) Student student,
            @RequestAttribute(required = false) Staff staff
    ) {
        // Logic inside the service handles if student or staff is replying
        return new ResponseEntity<>(discussionService.postReply(questionId, request, student, staff), HttpStatus.CREATED);
    }

    @PreAuthorize("hasAuthority('SUPPORT') or hasAuthority('ORG_ADMIN') or (hasAuthority('COURSE_EDITOR')")
    @PutMapping("/replies/{replyId}/accept")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Answer status updated"),
            @ApiResponse(responseCode = "401", description = "Unauthorized",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "403", description = "Forbidden: Insufficient permissions (Staff only)",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "Reply not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    public ResponseEntity<Void> acceptAnswer(@PathVariable Long replyId,
                                             @RequestParam boolean isAcceptedAnswer,
                                             @RequestAttribute(required = false) Staff staff) {
        discussionService.acceptAnswer(replyId, isAcceptedAnswer, staff);
        return ResponseEntity.ok().build();
    }
}
