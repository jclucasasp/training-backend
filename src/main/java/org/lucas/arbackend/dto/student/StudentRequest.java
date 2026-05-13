package org.lucas.arbackend.dto.student;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.util.ValidatedLabel;

@Data @Builder
public class StudentRequest {

    @NotNull(message = "Param 'studentNumber' missing or blank", groups = ValidatedLabel.OnCreate.class)
    private String studentNumber;

    @NotNull(message = "Param 'firstName' missing or blank", groups = ValidatedLabel.OnCreate.class)
    private String firstName;

    @NotNull(message = "Param 'lastName' missing or blank", groups = ValidatedLabel.OnCreate.class)
    private String lastName;

    @NotNull(message = "Param 'slug' missing or blank", groups = ValidatedLabel.OnCreate.class)
    private String slug;

}

