package org.lucas.arbackend.dto.student;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class EnrollmentResponse {
    private Long enrollmentId;
    private String studentNumber;
    private String courseName;
    private LocalDateTime enrolledAt;
    private BigDecimal currentTotalProgress;
}
