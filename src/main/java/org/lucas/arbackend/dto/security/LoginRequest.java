package org.lucas.arbackend.dto.security;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(name = "LoginRequest", description = "Payload layout representing primary security credentials needed to establish an authorized identity communication token session context link")
public class LoginRequest {

    @NotBlank(message = "Email address is required")
    @Email(message = "Please enter a valid email address")
    @Schema(description = "The registered authentication account login identifier link email copy text", example = "admin@acmecorp.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "Password cannot be empty")
    @Size(min = 8, max = 20, message = "Password must be between 8 and 20 characters long")
    @Schema(description = "The matching secret account passphrase character sequence mapped to the user identity profile instance link", example = "P@ssw0rd2026", minLength = 8, maxLength = 20, requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}