package org.lucas.arbackend.dto.security;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Builder
@Data
@AllArgsConstructor @NoArgsConstructor
@Schema(name = "ApiKeyResponse", description = "Structural output data model profiling active API token tracking attributes, generation checkpoints, and linked organization tiers status mapping")
public class ApiKeyResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "The unique internal database primary sequence index locating the parent multi-tenant entity", example = "104")
    private Long orgId;

    @Schema(description = "The full unredacted plaintext secret token string generated during initial setup. Note: Only populated on immediate creation checkpoints.", example = "org_sk_live_a1b2c3d4e5f6g7h8i9j0k")
    private String rawKey;

    @Schema(description = "The short, public alphanumeric identifier fragment used for basic credential isolation lookups", example = "org_sk_live")
    private String prefix;

    @Schema(description = "The secure cryptographically computed hash signature representation stored inside the primary storage block ledger", example = "8f3a1b2c4d5e6f7g8h9i0j...")
    private String hashedKey;

    @Schema(description = "ISO date-time checkpoint record tracking exactly when this credentials token instance structure initialization completed", example = "2026-01-15T08:30:00")
    private LocalDateTime createdAt;

    @Schema(description = "Flags whether the parent organization tenant maintains an active, non-delinquent subscription license layer entitlement runtime state", example = "true")
    private Boolean isSubscriptionActive;
}