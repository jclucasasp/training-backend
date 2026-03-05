package org.lucas.arbackend.dto.course.attachment;

import java.io.Serial;
import java.io.Serializable;

public record AttachmentResponse(
        Long id,
        String fileName,
        String fileType,
        String fileUrl
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
