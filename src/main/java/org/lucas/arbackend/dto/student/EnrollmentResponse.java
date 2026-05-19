package org.lucas.arbackend.dto.student;

import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record EnrollmentResponse (
        Long enrollmentId,
        String studentNumber,
        String courseName,
        LocalDateTime enrolledAt,
        BigDecimal currentTotalProgress
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
