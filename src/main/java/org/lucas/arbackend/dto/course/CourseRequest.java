package org.lucas.arbackend.dto.course;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.entity.course.ChapterQuiz;
import org.lucas.arbackend.entity.course.misc.DifficultyTypes;
import org.lucas.arbackend.entity.course.misc.StatusTypes;
import org.lucas.arbackend.util.ValidatedLabel;

import java.util.List;

@Data @Builder
public class CourseRequest {
    @NotBlank(message = "Assign a staff member via their email to the course", groups = ValidatedLabel.OnCreate.class)
    @Email
    private String staffEmail;

    @NotBlank(message = "Course fileName is required", groups = ValidatedLabel.OnCreate.class)
    private String name;

    @NotBlank(message = "Course shortDescription is required", groups = ValidatedLabel.OnCreate.class)
    private String shortDescription;

    @NotBlank(message = "Please specify who the intended course is for", groups = ValidatedLabel.OnCreate.class)
    private String intendedAudience;

    @NotBlank(message = "Course requirements is required", groups = ValidatedLabel.OnCreate.class)
    private String requirements;

    @NotBlank(message = "Course status is required", groups = ValidatedLabel.OnCreate.class)
    private StatusTypes status;

    @NotBlank(message = "Course learningObjectives is required", groups = ValidatedLabel.OnCreate.class)
    private String learningObjectives;

    @NotNull(message = "Course difficulty is required", groups = ValidatedLabel.OnCreate.class)
    private DifficultyTypes difficultyTypes; // BEGINNER, INTERMEDIATE, ADVANCED

    @NotBlank(message = "Course tags are required", groups = ValidatedLabel.OnCreate.class)
    private String tags;

    private String imageUrl;

    @NotNull(message = "Course courseModules are required", groups = ValidatedLabel.OnCreate.class)
    private List<CourseChapterRequest> chapters;

    private List<ChapterQuiz> quizzes;

}

