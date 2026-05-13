package org.lucas.arbackend.dto.QAndA;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "Param 'title' mission or blank", groups = ValidatedLabel.OnCreate.class)
    private String title;

    @NotBlank(message = "Param 'body' missing or blank", groups = ValidatedLabel.OnCreate.class)
    private String body;
}
