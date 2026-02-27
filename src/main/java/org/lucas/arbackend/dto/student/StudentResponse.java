package org.lucas.arbackend.dto.student;

import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;

@Builder
public record StudentResponse (
        Long id,
        String studentNumber,
        String firstName,
        String lastName
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
