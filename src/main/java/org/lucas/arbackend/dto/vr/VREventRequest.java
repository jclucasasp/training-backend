package org.lucas.arbackend.dto.vr;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.entity.vr.event.VREventType;
import org.lucas.arbackend.entity.vr.event.VRHandType;
import org.lucas.arbackend.util.ValidatedLabel;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
@Schema(name = "VREventRequest", description = "Individual VR telemetry event from headset")
public class VREventRequest {

    @NotNull(message = "Event type is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "Type of VR event", example = "GRAB", requiredMode = Schema.RequiredMode.REQUIRED)
    private VREventType eventType;

    @NotNull(message = "Timestamp is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "Event timestamp", example = "2026-06-23T14:30:15.123", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime timestamp;

    @Schema(description = "X position coordinate", example = "1.2345")
    private BigDecimal positionX;

    @Schema(description = "Y position coordinate", example = "0.5000")
    private BigDecimal positionY;

    @Schema(description = "Z position coordinate", example = "-2.1000")
    private BigDecimal positionZ;

    @Schema(description = "X rotation (Euler)", example = "0.0000")
    private BigDecimal rotationX;

    @Schema(description = "Y rotation (Euler)", example = "90.0000")
    private BigDecimal rotationY;

    @Schema(description = "Z rotation (Euler)", example = "0.0000")
    private BigDecimal rotationZ;

    @Schema(description = "Target object ID from SceneConfig", example = "valve_01")
    private String targetObjectId;

    @Schema(description = "Event duration in milliseconds", example = "1500")
    private Integer durationMs;

    @Schema(description = "Flexible JSON metadata for event-specific data", example = "{grabForce:0.8,teleportDestination:platform_2}")
    private String metadataJson;

    @Schema(description = "Hand used for interaction", example = "RIGHT", allowableValues = {"LEFT", "RIGHT", "BOTH", "HEAD"})
    private VRHandType hand;

    @Schema(description = "Sequence number for ordering/replay", example = "1847")
    private Long sequenceNumber;
}
