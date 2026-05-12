package org.lucas.arbackend.dto.course;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.dto.quiz.QuizRequest;
import org.lucas.arbackend.entity.course.ChapterQuiz;
import org.lucas.arbackend.entity.course.misc.DifficultyTypes;
import org.lucas.arbackend.entity.course.misc.StatusTypes;
import org.lucas.arbackend.util.ValidatedLabel;

import java.util.List;

@Data @Builder
public class CourseRequest {
    @NotBlank(message = "Param 'staffEmail' missing or blank", groups = ValidatedLabel.OnCreate.class)
    @Email
    private String staffEmail;

    @NotBlank(message = "Param 'name' for course missing or blank", groups = ValidatedLabel.OnCreate.class)
    private String name;

    @NotBlank(message = "Param 'shortDescription' missing or blank", groups = ValidatedLabel.OnCreate.class)
    private String shortDescription;

    @NotBlank(message = "Param 'intendedAudience' missing or blank", groups = ValidatedLabel.OnCreate.class)
    private String intendedAudience;

    @NotBlank(message = "Param 'requirements' missing or blank", groups = ValidatedLabel.OnCreate.class)
    private String requirements;

    @NotBlank(message = "Param 'status' missing or blank", groups = ValidatedLabel.OnCreate.class)
    private StatusTypes status;

    @NotBlank(message = "Param 'learningObjectives' missing or blank", groups = ValidatedLabel.OnCreate.class)
    private String learningObjectives;

    @NotNull(message = "Param 'difficulty' missing or blank", groups = ValidatedLabel.OnCreate.class)
    private DifficultyTypes difficulty; // BEGINNER, INTERMEDIATE, ADVANCED

    private String imageUrl;

    @NotBlank(message = "Param 'tags' missing or blank", groups = ValidatedLabel.OnCreate.class)
    private String tags;

    @NotEmpty(message = "List 'chapters' missing or blank", groups = ValidatedLabel.OnCreate.class)
    private List<CourseChapterRequest> chapters;

    private List<QuizRequest> quizzes;

}

