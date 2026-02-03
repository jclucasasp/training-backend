package org.lucas.arbackend.dto.organisation;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data @Builder
public class ProfileRequest {
    @NotNull(message = "Organisation name is required")
    private String orgName; // For the Profile

    @NotNull(message = "Registration number is required")
    private String regNumber; // For the Profile

    @NotNull(message = "VAT number is required")
    private String vatNumber; // For the Profile
}
