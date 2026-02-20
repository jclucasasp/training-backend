package org.lucas.arbackend.dto.security;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @Email(message = "Invalid email address")
    @Schema(example = "admin@org.com")
    private String email;

    @NotBlank()
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    @Schema(example = "password")
    private String password;
}
