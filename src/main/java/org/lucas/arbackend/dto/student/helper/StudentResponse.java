package org.lucas.arbackend.dto.student.helper;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data @Builder
public class StudentResponse {
    private Long id;
    private String studentNumber;
    private Long organisationId;
    private LocalDateTime createdAt;
    private LocalDateTime endedAt;
}
