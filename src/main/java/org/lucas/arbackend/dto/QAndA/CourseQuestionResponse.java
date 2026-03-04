package org.lucas.arbackend.dto.QAndA;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record CourseQuestionResponse(
        Long id, String title, String body, String studentName,
        boolean isResolved, LocalDateTime createdAt, List<ReplyResponse> replies
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
