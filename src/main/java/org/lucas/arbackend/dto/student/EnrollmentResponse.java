package org.lucas.arbackend.dto.student;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class EnrollmentResponse implements Serializable {
    private Long enrollmentId;
    private String studentNumber;
    private String courseName;
    private LocalDateTime enrolledAt;
    private BigDecimal currentTotalProgress;
}
