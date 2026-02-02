package org.lucas.arbackend.dto.student;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Data @Builder
public class EnrollmentResponse {
    private Long enrollmentId;
    private String courseName;
    private Double progressPercentage;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
}
