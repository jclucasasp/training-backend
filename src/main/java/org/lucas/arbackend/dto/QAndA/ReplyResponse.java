package org.lucas.arbackend.dto.QAndA;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(name = "ReplyResponse", description = "Structural schema representing a individual comment node mapping inside a forum thread hierarchy")
public record ReplyResponse(

        @Schema(description = "Unique entity identifier locating the reply record", example = "1024")
        Long id,

        @Schema(description = "The response text body content", example = "Make sure you updated the route tables on BOTH VPCs, not just the initiator VPC.")
        String body,

        @Schema(description = "The calculated display profile name of the message poster", example = "Instructor Jane Doe")
        String authorName,

        @Schema(description = "Identifies whether the author belongs to an administrative or teaching team role context", example = "true")
        boolean isStaff,

        @Schema(description = "True if this particular response statement has been pinned and accepted as a valid problem solution", example = "true")
        boolean isAccepted,

        @Schema(description = "ISO date-time verification entry tracking message publication events", example = "2026-06-01T11:42:00")
        LocalDateTime createdAt

) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}