package org.lucas.arbackend.dto.security;

import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @param rawKey ONLY shown once upon creation
 * @param prefix Shown later for identification (e.g. "sk_live_4a...")
 */
@Builder
public record ApiKeyResponse(Long orgId, String rawKey, String prefix, String hashedKey,
                             LocalDateTime createdAt) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

}
