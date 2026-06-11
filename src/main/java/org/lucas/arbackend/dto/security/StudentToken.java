package org.lucas.arbackend.dto.security;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

public class StudentToken implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "The unique internal database primary sequence index locating the parent multi-tenant entity", example = "104")
    private Long orgId;

    @Schema(description = "Flags whether the parent organization tenant maintains an active, non-delinquent subscription license layer entitlement runtime state", example = "true")
    private boolean isSubscriptionActive;

    @Schema(description = "Unique UUID V4 token string used for student authentication and session tracking", example = "8f3a1b2c4d5e6f7g8h9i0j...")
    private String studentToken;

    @Schema(description = "The unique identifier for the student/employee", example = "STU-100465")
    private Long studentNumber;

    @Schema(description = "The first name of the student", example = "Steward")
    private String studentName;

    @Schema(description = "The last name of the student", example = "Little")
    private String studentLastname;

    @Schema(description = "ISO date-time checkpoint record tracking exactly when this credentials token instance structure initialization completed", example = "2026-01-15T08:30:00")
    private LocalDateTime createdAt;
}
