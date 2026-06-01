package org.lucas.arbackend.dto.QAndA;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.lucas.arbackend.util.ValidatedLabel;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(name = "CourseQuestionRequest", description = "Payload layout required for a student to post a new question within a course or section discussion forum")
public class CourseQuestionRequest {

    @Schema(description = "The unique internal database ID of the course context", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long courseId;

    @Schema(description = "The unique internal database ID of the specific lesson section module where the question is being asked", example = "28", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sectionId;

    @NotBlank(message = "Question title is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "A brief, clear title summarizing the student's problem", example = "VPC Peering routes not propagating", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank(message = "Question body cannot be empty", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The detailed markdown or text copy explaining the problem, steps taken, or error codes encountered", example = "I followed the steps in Module 1, but my instances still cannot ping across regions. Here is my route table...", requiredMode = Schema.RequiredMode.REQUIRED)
    private String body;
}