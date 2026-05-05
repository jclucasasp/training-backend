package org.lucas.arbackend.dto.quiz;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public record QuizResponse(
        Long id,
        String title,
        Integer maxAttempts,
        Integer passingScore,
        List<QuestionResponse> questions
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
