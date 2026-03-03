package org.lucas.arbackend.dto.course.misc;

import java.io.Serial;
import java.io.Serializable;

public record AttachmentResponse(
        Long id,
        String name,
        String url
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
