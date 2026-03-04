package org.lucas.arbackend.dto.quiz;

import java.io.Serial;
import java.io.Serializable;

public record QuizResultResponse(int score, boolean passed) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
