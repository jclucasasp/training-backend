package org.lucas.arbackend.dto.vr.session;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Schema(name = "VRSessionResponse", description = "VR training session summary response")
public record VRSessionResponse(
    @Schema(description = "Unique session identifier", example = "101")
    Long id,

    @Schema(description = "Student number", example = "STU-12345")
    String studentNumber,

    @Schema(description = "Student full name", example = "John Doe")
    String studentName,

    @Schema(description = "Section ID", example = "28")
    Long sectionId,

    @Schema(description = "Section title", example = "Module 1: Emergency Valve Shutdown")
    String sectionTitle,

    @Schema(description = "Device identifier", example = "Oculus-Quest3-A1B2C3D4")
    String deviceId,

    @Schema(description = "Headset model", example = "Quest 3")
    String headsetModel,

    @Schema(description = "The version of the current scene", example = "1")
    Long sceneVersionId,

    @Schema(description = "Session start time", example = "2026-06-23T14:30:00")
    LocalDateTime startedAt,

    @Schema(description = "Session end time", example = "2026-06-23T14:35:00")
    LocalDateTime endedAt,

    @Schema(description = "Duration in seconds", example = "300")
    Integer durationSeconds,

    @Schema(description = "Comfort rating (1-5)", example = "4")
    Integer comfortRating,

    @Schema(description = "Motion sickness reported", example = "false")
    Boolean motionSicknessReported,

    @Schema(description = "Computed session quality score", example = "0.95")
    BigDecimal sessionQualityScore,

    @Schema(description = "Average FPS", example = "72.5")
    BigDecimal avgFps,

    @Schema(description = "Frame drops", example = "12")
    Integer frameDropCount,

    @Schema(description = "Tracking loss events", example = "0")
    Integer trackingLossCount,

    @Schema(description = "Total interactions", example = "45")
    Integer interactionCount,

    @Schema(description = "Hints requested", example = "2")
    Integer hintRequestCount,

    @Schema(description = "Failure triggers", example = "1")
    Integer failureCount,

    @Schema(description = "Completion condition met", example = "true")
    Boolean completionConditionMet,

    @Schema(description = "Completion time in ms", example = "185000")
    Long completionTimeMs
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
