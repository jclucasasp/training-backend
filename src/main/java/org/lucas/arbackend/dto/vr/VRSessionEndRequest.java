package org.lucas.arbackend.dto.vr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data @Builder
@Schema(name = "VRSessionEndRequest", description = "Payload to finalize a VR training session with telemetry summary")
public class VRSessionEndRequest {

    @Schema(description = "Trainee comfort rating post-session (1-5)", example = "4")
    private Integer comfortRating;

    @Schema(description = "Whether motion sickness was reported", example = "false")
    private Boolean motionSicknessReported;

    @Schema(description = "Average frames per second during session", example = "72.5")
    private BigDecimal avgFps;

    @Schema(description = "Total frame drops experienced", example = "12")
    private Integer frameDropCount;

    @Schema(description = "Tracking loss events count", example = "0")
    private Integer trackingLossCount;

    @Schema(description = "Total interaction count (grabs, presses, etc.)", example = "45")
    private Integer interactionCount;

    @Schema(description = "Number of hints requested", example = "2")
    private Integer hintRequestCount;

    @Schema(description = "Number of failure triggers", example = "1")
    private Integer failureCount;

    @Schema(description = "Whether the completion condition was met", example = "true")
    private Boolean completionConditionMet;

    @Schema(description = "Time to complete in milliseconds (null if not completed)", example = "185000")
    private Long completionTimeMs;
}
