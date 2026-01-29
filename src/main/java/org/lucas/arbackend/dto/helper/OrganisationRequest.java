package org.lucas.arbackend.dto.helper;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.validator.constraints.Length;

@AllArgsConstructor @NoArgsConstructor
@Getter @Setter
@Builder
public class OrganisationRequest {
    @NotNull(message = "Email is required")
    @Email(message = "Email is not valid")
    private String email;
    @NotNull(message = "Password is required")
    @Length(message = "Password must be at least 8 characters", min = 8)
    private String password;
}
