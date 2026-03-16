package org.lucas.arbackend.dto.QAndA;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public record ReplyResponse(
        Long id,
        String body, // Reply to the question
        String authorName, // If the reply is from a student or staff member
        boolean isStaff, // This can be used to filter
        boolean isAccepted, // If the reply is accepted as the answer. Only staff should be able to pin
        LocalDateTime createdAt
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
