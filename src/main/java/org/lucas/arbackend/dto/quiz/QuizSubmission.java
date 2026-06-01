package org.lucas.arbackend.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

@Schema(name = "QuizSubmission", description = "A simple key-value structured entry summary mapping targeted assessment values")
public record QuizSubmission(

        @Schema(description = "A mapping collection tree where each Key represents the unique internal question sequence identifier and the corresponding Value represents the selected choice option sequence identifier",
                example = "{\"250\": 4001, \"251\": 4004}")
        Map<Long, Long> answers

) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}