package org.lucas.arbackend.dto.helper;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class SignUpRequest {
    // Organisation Details
    @NotNull (message = "Email is required")
    @Email (message = "Must be a valid email address")
    private String email;

    @NotNull (message = "Password is required")
    @Length (message = "Password must not be at least 8 characters long and can not exceed 20 characters", min = 8, max = 20)
    private String password;

    // Profile Details
    @NotNull (message = "Organisation name is required")
    private String orgName;

    @NotNull (message = "Registration number is required")
    private String registrationNumber;

    @NotNull (message = "Vat number is required")
    private String vatNumber;
}
