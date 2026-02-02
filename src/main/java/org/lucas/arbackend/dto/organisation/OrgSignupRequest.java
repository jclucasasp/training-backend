package org.lucas.arbackend.dto.organisation;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Data @Builder
public class OrgSignupRequest {
    @Email(message = "Must be a valid email address")
    @NotNull(message = "Email address is required")
    private String email;

    @NotNull(message = "Password is required")
    private String password;

    @NotNull(message = "Organisation name is required")
    private String orgName;

    @NotNull(message = "Registration number is required")
    private String registrationNumber;

    @NotNull(message = "VAT number is required")
    private String vatNumber;

    private Long initialPlanId; // Optional, defaults to Free/Basic
}

