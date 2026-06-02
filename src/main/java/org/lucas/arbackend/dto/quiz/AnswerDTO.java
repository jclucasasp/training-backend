package org.lucas.arbackend.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
@Schema(name = "AnswerDTO", description = "A student's individual submitted choice response mapping to a single quiz question assessment block")
public class AnswerDTO  {

    @Schema(description = "The unique internal primary identifier tracking the targeted quiz question", example = "250", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long questionId;

    @Schema(description = "The unique internal database identifier matching the option element choice picked by the user", example = "4001", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long selectedOptionId;
}