package org.lucas.arbackend.dto.organisation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.entity.security.RoleTypes;
import org.lucas.arbackend.util.ValidatedLabel;

@Data @Builder
@Schema(name = "StaffRequest", description = "Payload layout required to provision or update an administrative, editor, or support staff user account within a tenant context")
public class StaffRequest {

    @NotBlank(groups = ValidatedLabel.OnCreate.class, message = "First name is required")
    @Size(min = 3, max = 20, message = "First name must be between 3 and 20 characters long")
    @Schema(description = "The given name of the staff member", example = "Jane", minLength = 3, maxLength = 20, requiredMode = Schema.RequiredMode.REQUIRED)
    private String firstName;

    @NotBlank(groups = ValidatedLabel.OnCreate.class, message = "Last name is required")
    @Size(min = 3, max = 20, message = "Last name must be between 3 and 20 characters long")
    @Schema(description = "The family name or surname of the staff member", example = "Doe", minLength = 3, maxLength = 20, requiredMode = Schema.RequiredMode.REQUIRED)
    private String lastName;

    @NotBlank(groups = ValidatedLabel.OnCreate.class, message = "Email address is required")
    @Email(message = "Please enter a valid email address")
    @Schema(description = "Unique corporate authentication and contact email address for the staff member", example = "jane.doe@acmeinstitute.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @Pattern(
            regexp = "^(\\+27|0)[6-8][0-9]{8}$",
            message = "Invalid mobile number format. Use 07x/08x... or +277x/278x...")
    @Schema(description = "Direct telephone contact link adhering to South African mobile structural formats", example = "0721234567", pattern = "^(\\+27|0)[6-8][0-9]{8}$")
    private String contactNumber;

    @NotBlank(groups = ValidatedLabel.OnCreate.class, message = "Password is required")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    @Schema(description = "Secure credential access passphrase for the staff member profile login context", example = "St@ffSecure2026", minLength = 8, maxLength = 20, requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @NotNull(message = "Staff role classification is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The authorization role defining systemic access hierarchy tiers", example = "COURSE_EDITOR", requiredMode = Schema.RequiredMode.REQUIRED)
    private RoleTypes role;
}