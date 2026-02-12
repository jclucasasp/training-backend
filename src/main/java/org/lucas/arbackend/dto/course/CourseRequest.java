package org.lucas.arbackend.dto.course;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.entity.course.DifficultyTypes;
import org.lucas.arbackend.entity.course.Module;

import java.util.List;
import java.util.Set;

@Data @Builder
public class CourseRequest {
    @NotNull(message = "Course name is required")
    private String name;

    @NotNull(message = "Course description is required")
    private String description;

    @NotNull(message = "Course difficulty is required")
    private DifficultyTypes difficultyTypes; // BEGINNER, INTERMEDIATE, ADVANCED

    @NotNull(message = "Course tags are required")
    private String tags;

    private String imageUrl;

    @NotNull(message = "Course modules are required")
    private Set<Module> modules;
}

