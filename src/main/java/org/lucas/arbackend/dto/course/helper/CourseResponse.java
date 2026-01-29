package org.lucas.arbackend.dto.course.helper;

import lombok.*;

import java.time.LocalDateTime;

@Builder @Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class CourseResponse {
    private Long id;
    private String name;
    private String description;
    private String difficulty;
     private LocalDateTime createdAt;
     private LocalDateTime endedAt;
}
