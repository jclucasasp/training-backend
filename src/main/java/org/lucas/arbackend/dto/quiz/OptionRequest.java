package org.lucas.arbackend.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
@Schema(name = "OptionRequest", description = "Payload layout for a single choice option inside an assessment quiz question")
public class OptionRequest {

    @NotBlank(message = "Option choice text is required")
    @Schema(description = "The display text for this answer option choice", example = "Virtual Private Cloud (VPC)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String text;

    @NotNull(message = "Specify whether this option choice is correct or incorrect")
    @Schema(description = "Flags whether this option represents a correct solution answer to the question", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean correct;
}