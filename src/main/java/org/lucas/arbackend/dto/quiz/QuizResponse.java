package org.lucas.arbackend.dto.quiz;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public record QuizResponse(
        Long id,
        String title,
        Integer maxAttempts,
        Integer passingScore,
        Long courseId,
        List<Long> chapterIds,
        List<String> studentNumbers
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
