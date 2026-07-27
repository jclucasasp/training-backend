package org.lucas.arbackend.dto.vr.competency;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.lucas.arbackend.entity.vr.competency.MetricType;

import java.io.Serial;
import java.io.Serializable;

@Builder
public record CompetencyCriterionResponse(
        Long id,
        @Schema(description = "The description of the assessment criteria", example = "Fire extinguisher usage")
        String description,
        @Schema(description = "The type of metric to use for the criterion", example = "TIME, ACCURACY, SEQUENCE, PRESENCE")
        MetricType metricType,
        @Schema(description = "The value of the metric to use for the criterion", example = "10")
        String thresholdValue,
        @Schema(description = "The description of the criterion", example = "The user must use the fire extinguisher within 10 seconds")
        Double weight
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
