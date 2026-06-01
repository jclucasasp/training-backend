package org.lucas.arbackend.dto.QAndA;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Schema(name = "CourseQuestionResponse", description = "The structural payload returned when displaying a forum thread question accompanied by all nested student or staff replies")
public record CourseQuestionResponse(

        @Schema(description = "Unique resource entity primary identifier", example = "501")
        Long id,

        @Schema(description = "The summary title text string of the forum thread", example = "VPC Peering routes not propagating")
        String title,

        @Schema(description = "The text copy details describing the student issue", example = "I followed the steps in Module 1, but my instances still cannot ping...")
        String body,

        @Schema(description = "The display name of the student who initiated the question thread", example = "Alex Johnson")
        String studentName,

        @Schema(description = "Flags whether a staff member or user has marked this topic discussion as solved", example = "false")
        boolean isResolved,

        @Schema(description = "ISO timestamp record detailing when the thread was created", example = "2026-06-01T10:15:30")
        LocalDateTime createdAt,

        @Schema(description = "Array containing all replies, answers, or comments posted underneath this thread node")
        List<ReplyResponse> replies

) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}