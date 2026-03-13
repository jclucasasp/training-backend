package org.lucas.arbackend.dto.quiz;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.lucas.arbackend.util.ValidatedLabel;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizRequest {
    @NotBlank(message = "Title cannot be blank", groups = ValidatedLabel.OnCreate.class)
    private  String title;

    @NotBlank(message = "Max attempts cannot be blank", groups = ValidatedLabel.OnCreate.class)
    private Integer maxAttempts;

    @NotBlank(message = "Passing score cannot be blank", groups = ValidatedLabel.OnCreate.class)
    private Integer passingScore;

    private Long courseId;
    private Long chapterId;
}
