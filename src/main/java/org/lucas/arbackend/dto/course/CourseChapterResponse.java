package org.lucas.arbackend.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.lucas.arbackend.dto.quiz.QuizResponse;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Builder
@Schema(name = "CourseChapterResponse", description = "The structural payload representing an individual chapter inside a course context summary")
public record CourseChapterResponse(
@Schema(description = "Unique database entity identifier for the chapter", example = "36")
        long id,

        @Schema(description = "The display name of the chapter", example = "Chapter 1: The Basics")
        String name,

        @Schema(description = "The content summary detailing the scope of this chapter", example = "An overview of cloud computing history.")
        String summary,

        @Schema(description = "The operational publishing status", example = "DRAFT")
        String status,

        @Schema(description = "The cumulative aggregate duration of all underlying sections in minutes", example = "35")
        Integer totalTimeInMinutes,

        @Schema(description = "Collection of modular lessons and sections linked to this chapter node")
        List<ChapterSectionResponse> sections,

        @Schema(description = "Collection of assessment chapterQuizzes bound directly to this chapter resource node")
        List<QuizResponse> chapterQuizzes
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
