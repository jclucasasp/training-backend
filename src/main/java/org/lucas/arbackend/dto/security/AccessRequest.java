package org.lucas.arbackend.dto.security;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccessRequest {
    @NotNull (message = "Must include an API key for access")
    private String apiKey;

    @NotNull (message = "Must include a student number for access")
    private String username;
    private String studentNumber;
}
