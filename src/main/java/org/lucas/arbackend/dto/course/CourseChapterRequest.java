package org.lucas.arbackend.dto.course;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.entity.course.misc.StatusTypes;
import org.lucas.arbackend.util.ValidatedLabel;

import java.util.List;

@Data @Builder
@Schema(name = "CourseChapterRequest", description = "Payload required to create or update a single chapter within a course curriculum")
public class CourseChapterRequest {
    @JsonIgnore
    private Long id;

    @NotNull(message = "Chapter name missing or blank", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The display name of the chapter", example = "Chapter 1: The Basics", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotNull(message = "Chapter summary missing or blank", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "A brief summary or outline of what is covered in this chapter", example = "An overview of cloud computing history.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String summary;

    @NotNull(message = "Chapter status missing or blank", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The editorial publication state of the chapter", example = "DRAFT", requiredMode = Schema.RequiredMode.REQUIRED)
    private StatusTypes status;

    @NotEmpty(message = "A chapter must contain at least one section", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The individual educational modules or lessons contained within this chapter", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<ChapterSectionRequest> sections;

    @Schema(description = "Array of existing unique quiz IDs assigned to assess knowledge at the end of this chapter", example = "[1, 2]")
    private List<Long> quizIds;
}
