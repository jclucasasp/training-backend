package org.lucas.arbackend.dto.helper;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ProfileRequest {

    @NotNull (message = "Organisation name is required")
    private String orgName;

    @NotNull (message = "Registration number is required")
    private String registrationNumber;

    @NotNull (message = "Vat number is required")
    private String vatNumber;
}
