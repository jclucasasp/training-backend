package org.lucas.arbackend.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.entity.security.Role;
import org.lucas.arbackend.util.ValidatedLabel;

@Data @Builder
@Schema(name = "StudentRequest", description = "Payload layout required to provision or synchronize an individual student enrollment identity within a tenant organization")
public class StudentRequest {

    @NotBlank(message = "First name is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The given name of the student", example = "Lucas", requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank(message = "Last name is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The family name or surname of the student", example = "Devan", requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @NotBlank(groups = ValidatedLabel.OnCreate.class, message = "Email address is required")
    @Schema(description = "The email address of the student", example = "lucas@devan.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @Email(message = "Please enter a valid email address")
    private String email;

    @Schema(description = "The password of the student", example = "password", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    private String password;

//    @NotBlank(message = "URL slug is required", groups = ValidatedLabel.OnCreate.class)
//    @Schema(description = "A unique web-safe lowercase hyphenated string token used for descriptive profile routing paths", example = "cloud-architecture-foundations", requiredMode = Schema.RequiredMode.REQUIRED)
//    private String slug;
}