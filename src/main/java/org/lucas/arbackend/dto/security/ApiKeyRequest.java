package org.lucas.arbackend.dto.security;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ApiKeyRequest {
    @NotNull (message = "Param 'apiKey' missing or blank")
    private String apiKey;

    private String username;
    @NotNull (message = "Param 'studentNumber' missing or blank")
    private String studentNumber;
}
