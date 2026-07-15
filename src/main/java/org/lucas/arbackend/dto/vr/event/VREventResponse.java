package org.lucas.arbackend.dto.vr.event;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Schema(name = "VREventResponse", description = "Individual VR telemetry event response")
public record VREventResponse(
    @Schema(description = "Unique event identifier", example = "5001")
    Long id,

    @Schema(description = "Session ID", example = "101")
    Long sessionId,

    @Schema(description = "Event type", example = "GRAB")
    String eventType,

    @Schema(description = "Event timestamp", example = "2026-06-23T14:30:15.123")
    LocalDateTime timestamp,

    @Schema(description = "X position", example = "1.2345")
    BigDecimal positionX,

    @Schema(description = "Y position", example = "0.5000")
    BigDecimal positionY,

    @Schema(description = "Z position", example = "-2.1000")
    BigDecimal positionZ,

    @Schema(description = "X rotation", example = "0.0000")
    BigDecimal rotationX,

    @Schema(description = "Y rotation", example = "90.0000")
    BigDecimal rotationY,

    @Schema(description = "Z rotation", example = "0.0000")
    BigDecimal rotationZ,

    @Schema(description = "Target object ID", example = "valve_01")
    String targetObjectId,

    @Schema(description = "Duration in ms", example = "1500")
    Integer durationMs,

    @Schema(description = "Metadata JSON", example = "{grabForce:0.8}")
    String metadataJson,

    @Schema(description = "Hand used", example = "RIGHT")
    String hand,

    @Schema(description = "Sequence number", example = "1847")
    Long sequenceNumber
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
