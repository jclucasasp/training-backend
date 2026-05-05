package org.lucas.arbackend.dto.quiz;

import java.io.Serial;
import java.io.Serializable;

public record OptionResponse(Long id, String text, boolean correct) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
