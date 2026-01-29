package org.lucas.arbackend.dto.course.helper;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CourseRequest {

    @NotNull(message = "Course name is required")
    private String name;

    @NotNull(message = "Course must have a description")
    private String description;

    @NotNull(message = "Course must have a difficulty")
    private String difficulty;
}
