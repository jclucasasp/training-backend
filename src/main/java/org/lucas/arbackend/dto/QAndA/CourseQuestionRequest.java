package org.lucas.arbackend.dto.QAndA;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.lucas.arbackend.util.ValidatedLabel;

import java.io.Serial;
import java.io.Serializable;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CourseQuestionRequest {
    private Long courseId;
    private Long sectionId;

    @NotBlank(message = "Title should not be blank", groups = ValidatedLabel.OnCreate.class)
    private String title;

    @NotBlank(message = "Body should not be blank", groups = ValidatedLabel.OnCreate.class)
    private String body;
}
