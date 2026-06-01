package org.lucas.arbackend.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.util.ValidatedLabel;

@Data @Builder
@Schema(name = "StudentRequest", description = "Payload layout required to provision or synchronize an individual student enrollment identity within a tenant organization")
public class StudentRequest {

    @NotBlank(message = "Student institutional tracking number is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The unique institutional registration identity tracking code assigned to the student", example = "STU20268841", requiredMode = Schema.RequiredMode.REQUIRED)
    private String studentNumber;

    @NotBlank(message = "First name is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The given name of the student", example = "Lucas", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank(message = "Last name is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The family name or surname of the student", example = "Devan", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @NotBlank(message = "URL slug is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "A unique web-safe lowercase hyphenated string token used for descriptive profile routing paths", example = "lucas-devan", requiredMode = Schema.RequiredMode.REQUIRED)
    private String slug;
}