package org.lucas.arbackend.dto.security;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data @Builder
public class ApiKeyResponse implements Serializable {
    private String rawKey; // ONLY shown once upon creation
    private String prefix; // Shown later for identification (e.g. "sk_live_4a...")
    private LocalDateTime createdAt;
}
