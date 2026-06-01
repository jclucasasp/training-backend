package org.lucas.arbackend.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter @Builder
@Schema(name = "QuestionRequest", description = "Payload layout required to provision an assessment quiz question node")
public class QuestionRequest {

    @NotBlank(message = "Question text content is required")
    @Schema(description = "The actual question prompt or problem description presented to the user", example = "What does the abbreviation VPC stand for?", requiredMode = Schema.RequiredMode.REQUIRED)
    private String text;

    @NotBlank(message = "Question evaluation type classification is required")
    @Schema(description = "The classification format style determining answer constraints", example = "MULTIPLE_CHOICE", allowableValues = {"MULTIPLE_CHOICE", "TRUE_FALSE", "SINGLE_ANSWER"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String type;

    @NotEmpty(message = "A question must contain at least one option choice selection")
    @Schema(description = "Collection array detailing all answer choice configurations linked to this question prompt", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<OptionRequest> options;
}