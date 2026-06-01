package org.lucas.arbackend.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Schema(name = "QuizResponse", description = "The structural endpoint representation returning aggregated evaluation properties metadata and question nodes layout")
public record QuizResponse(

        @Schema(description = "Unique internal database generated identifier sequence indexing key", example = "55")
        Long id,

        @Schema(description = "The display title of the quiz assessment module", example = "VPC Networking Fundamentals Essentials")
        String title,

        @Schema(description = "The count cap of active testing session attempts permitted per student profile", example = "3")
        Integer maxAttempts,

        @Schema(description = "The integer target minimum matching mark boundary defining passing states", example = "80")
        Integer passingScore,

        @Schema(description = "The organized questions listing maps associated with this testing entity context node")
        List<QuestionResponse> questions

) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}