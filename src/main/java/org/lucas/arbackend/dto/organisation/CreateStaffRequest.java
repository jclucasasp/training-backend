package org.lucas.arbackend.dto.organisation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data @Builder
public class CreateStaffRequest {

    @Email(message = "Must be a valid email address")
    @NotNull(message = "Staff email is required")
    private String email;

    @NotNull(message = "Staff password is required")
    private String password;

    @NotNull(message = "Staff role is required")
    private String role;

}

