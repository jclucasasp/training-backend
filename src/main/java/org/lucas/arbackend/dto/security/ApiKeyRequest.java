package org.lucas.arbackend.dto.security;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ApiKeyRequest {
    @NotNull (message = "Must include an API key for access")
    private String apiKey;

    private String username;
    @NotNull (message = "Must include a student number for access")
    private String studentNumber;
}
