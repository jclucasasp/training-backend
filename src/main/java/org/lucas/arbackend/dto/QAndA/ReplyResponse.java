package org.lucas.arbackend.dto.QAndA;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public record ReplyResponse(
        Long id, String body, String authorName,
    boolean isStaff, boolean isAccepted, LocalDateTime createdAt
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
