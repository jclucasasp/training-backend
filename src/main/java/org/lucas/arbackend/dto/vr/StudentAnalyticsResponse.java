package org.lucas.arbackend.dto.vr;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Builder
@Schema(name = "StudentAnalyticsResponse", description = "Aggregated VR training analytics for a student")
public record StudentAnalyticsResponse(
    @Schema(description = "Student number", example = "STU-12345")
    String studentNumber,

    @Schema(description = "Total completed VR sessions", example = "12")
    long totalCompletedSessions,

    @Schema(description = "Average session quality score", example = "0.87")
    BigDecimal averageQualityScore,

    @Schema(description = "Motion sickness reports count", example = "1")
    long motionSicknessReports
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
