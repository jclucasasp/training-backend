package org.lucas.arbackend.dto.student;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.util.ValidatedLabel;

@Data @Builder
public class StudentRequest {

    @NotNull(message = "Student number is required", groups = ValidatedLabel.OnCreate.class)
    private String studentNumber;

    @NotNull(message = "First name is required", groups = ValidatedLabel.OnCreate.class)
    private String firstName;

    @NotNull(message = "Last name is required", groups = ValidatedLabel.OnCreate.class)
    private String lastName;

    @NotNull(message = "Course slug is required", groups = ValidatedLabel.OnCreate.class)
    private String slug;

}

