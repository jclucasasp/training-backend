package org.lucas.arbackend.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.lucas.arbackend.util.ValidatedLabel;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(name = "QuizRequest", description = "Payload layout required to provision or update a lesson module evaluation quiz")
public class QuizRequest {

    @NotBlank(message = "Quiz title is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The display title of the quiz assessment module", example = "VPC Networking Fundamentals Essentials", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotNull(message = "Maximum allowed attempts must be specified", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The threshold tracking maximum retries permitted per student assignment. Defaults to 3.", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer maxAttempts;

    @NotNull(message = "Passing score threshold is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The minimum percentage scale value required to achieve a passing evaluation baseline", example = "80", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer passingScore;

    @NotNull(message = "Associated course identification index is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The unique primary database sequencing key linking this quiz to a parent course tree context", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long courseId;

    @NotNull(message = "Associated chapter identification index is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The unique primary database sequencing key linking this quiz directly to a specific chapter module group", example = "45", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long chapterId;

    @NotEmpty(message = "A quiz must contain at least one question node", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The sequential list collection tree comprising evaluation assessment question elements layouts", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<QuestionRequest> questions;
}