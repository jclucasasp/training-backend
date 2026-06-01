package org.lucas.arbackend.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(
        name = "StudentQuizResponse",
        description = "A lightweight payload representing a quiz assigned to or associated with a student, completely decoupled from lazy-loaded database proxy structures."
)
public record StudentQuizResponse(
        @Schema(description = "Unique database sequencing identifier for the student quiz instance record", example = "42")
        Long id,

        @Schema(description = "The database primary key index linking to the core Quiz definition entity module template", example = "55")
        Long quizId,

        @Schema(description = "The timestamp marking exactly when this quiz profile instance track records assignment to the student identity", example = "2026-06-01T10:15:30")
        LocalDateTime assignedAt
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}