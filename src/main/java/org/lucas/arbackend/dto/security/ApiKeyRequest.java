package org.lucas.arbackend.dto.security;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Data @Builder
@Schema(name = "ApiKeyRequest", description = "Payload layout required to validate or swap an API security token against a specific multi-tenant student profile")
public class ApiKeyRequest {

    @NotBlank(message = "API key is required")
    @Schema(description = "The secret multi-tenant tracking token string utilized for secure external communication channel routing", example = "sk_aa6cd1b1bec04dda903e30229d2c0c0525d35997bae142a09e6192013b46f33c", requiredMode = Schema.RequiredMode.REQUIRED)
    private String apiKey;

    @Schema(description = "The unique authentication identity name linked with the account profile", example = "lucas.dev")
    private String username;

    @NotBlank(message = "Student number is required")
    @Schema(description = "The official institutional enrollment sequence tracking code identifier linked with the active student session", example = "STU20268841", requiredMode = Schema.RequiredMode.REQUIRED)
    private String studentNumber;
}