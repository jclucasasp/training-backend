package org.lucas.arbackend.dto.quiz;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

public record QuestionResponse(
        Long id, String text, List<OptionResponse> options
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
