package org.lucas.arbackend.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Schema(name = "QuestionResponse", description = "The structural output schema detailing individual query items embedded across quiz modules maps")
public record QuestionResponse(

        @Schema(description = "Unique primary sequence record identifier referencing this single item prompt", example = "250")
        Long id,

        @Schema(description = "The core instructional text prompt or scenario block presented directly to the user", example = "What does the abbreviation VPC stand for?")
        String text,

        @Schema(description = "The classification strategy configuration checking entry parameters constraints structures", example = "MULTIPLE_CHOICE")
        String type,

        @Schema(description = "The child option choice blocks matching potential user selection targets graphs")
        List<OptionResponse> options

) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}