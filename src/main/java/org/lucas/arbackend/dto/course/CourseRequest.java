package org.lucas.arbackend.dto.course;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.entity.course.DifficultyTypes;
import org.lucas.arbackend.util.ValidatedLabel;

import java.util.Set;

@Data @Builder
public class CourseRequest {
    @NotBlank(message = "Assign a staff member via their email to the course", groups = ValidatedLabel.OnCreate.class)
    @Email
    private String staffEmail;

    @NotBlank(message = "Course name is required")
    private String name;

    @NotBlank(message = "Course learningObjectives is required")
    private String learningObjectives;

    @NotNull(message = "Course difficulty is required")
    private DifficultyTypes difficultyTypes; // BEGINNER, INTERMEDIATE, ADVANCED

    @NotBlank(message = "Course tags are required")
    private String tags;

    private String imageUrl;

    @NotNull(message = "Course courseModules are required")
    private Set<CourseChapterRequest> chapters;
}

