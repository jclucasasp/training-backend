package org.lucas.arbackend.dto.helper;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Builder
public class OrganisationRequest {
    // Organisation Signup Details
    @NotNull (message = "Email is required")
    @Email (message = "Email is not valid")
    private String email;

    @NotNull (message = "Password is required")
    @Length (message = "Password must not be at least 8 characters long and can not exceed 20 characters", min = 8, max = 20)
    private String password;
}
