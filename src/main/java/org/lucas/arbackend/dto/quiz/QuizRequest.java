package org.lucas.arbackend.dto.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.lucas.arbackend.util.ValidatedLabel;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizRequest {
    @NotBlank(message = "Param 'title' is missing of blank", groups = ValidatedLabel.OnCreate.class)
    private  String title;

    @NotNull(message = "Param 'maxAttempts' missing or null. Defaults to 3", groups = ValidatedLabel.OnCreate.class)
    private Integer maxAttempts;

    @NotNull(message = "Param 'passingScore' missing or null", groups = ValidatedLabel.OnCreate.class)
    private Integer passingScore;

    @NotNull(message = "Param 'courseId' missing or null", groups = ValidatedLabel.OnCreate.class)
    private Long courseId;

    @NotNull(message = "Param 'chapterId' missing or null", groups = ValidatedLabel.OnCreate.class)
    private Long chapterId;

    @NotEmpty(message = "List 'questions' missing or empty", groups = ValidatedLabel.OnCreate.class)
    private List<QuestionRequest> questions;
}
