package org.lucas.arbackend.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Schema(name = "QuizResultResponse", description = "Immediate tracking receipt evaluation summary data payload returned following a submission transaction processing checkpoint")
public record QuizResultResponse(

        @Schema(description = "The unique internal primary tracking index pinpointing the generated history sheet record instance reference", example = "101")
        Long id,

        @Schema(description = "The final precision calculated percentage accuracy mark calculated following grading execution", example = "85.00")
        BigDecimal score,

        @Schema(description = "Systemic indicator marking whether the calculated grade metric met or exceeded required passing thresholds requirements parameters", example = "true")
        boolean passed

) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}