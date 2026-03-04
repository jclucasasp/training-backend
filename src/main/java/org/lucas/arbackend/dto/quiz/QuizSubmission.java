package org.lucas.arbackend.dto.quiz;

import java.io.Serial;
import java.io.Serializable;
import java.util.Map;

public record QuizSubmission(Map<Long, Long> answers) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
