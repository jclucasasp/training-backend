package org.lucas.arbackend.dto.organisation;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.lucas.arbackend.util.ValidatedLabel;

@Data @Builder
public class StaffRequest {

    @NotBlank(groups = ValidatedLabel.OnCreate.class)
    @Size(min = 3, max = 20, message = "Contact person firstname must be between 3 and 20 characters long")
    private String firstName;

    @NotBlank(groups = ValidatedLabel.OnCreate.class)
    @Size(min = 3, max = 20, message = "Contact person firstname must be between 3 and 20 characters long")
    private String lastName;

    @NotBlank(groups = ValidatedLabel.OnCreate.class)
    @Email(message = "Must be a valid email address")
    private String email;

    @Pattern(
            regexp = "^(\\+27|0)[6-8][0-9]{8}$",
            message = "Invalid South African mobile number. Use 07x/08x... or +277x/278x...")
    private String contactNumber;

    @NotBlank(groups = ValidatedLabel.OnCreate.class)
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    private String password;

    @NotNull(message = "Staff role is required", groups = ValidatedLabel.OnCreate.class)
    private RoleTypes role;

}

