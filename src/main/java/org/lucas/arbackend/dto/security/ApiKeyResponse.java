package org.lucas.arbackend.dto.security;

import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor @NoArgsConstructor
public class ApiKeyResponse implements Serializable {
    Long orgId;
    String rawKey;
    String prefix;
    String hashedKey;
    LocalDateTime createdAt;

    @Serial
    private static final long serialVersionUID = 1L;

}
