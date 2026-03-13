package org.lucas.arbackend.dto.quiz;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

public record QuizResultResponse(Long id, BigDecimal score, boolean passed) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
