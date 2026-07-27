package org.lucas.arbackend.dto.vr.competency;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lucas.arbackend.entity.vr.competency.MetricType;

@Data @Builder
@AllArgsConstructor @NoArgsConstructor
public class CompetencyCriterionRequest {
    @Schema(description = "The name of the criterion", example = "Fire extinguisher usage")
    @NotBlank(message = "Criterion name is required")
    private String description;

    @Schema(description = "The type of metric to use for the criterion", example = "TIME, ACCURACY, SEQUENCE, PRESENCE")
    @NotNull(message = "Criterion metric type is required")
    private MetricType metricType;

    @Schema(description = "The value of the metric to use for the criterion", example = "10")
    @NotNull(message = "Criterion metric value is required")
    private String thresholdValue;

    @Schema(description = "The description of the criterion", example = "The user must use the fire extinguisher within 10 seconds")
    @NotBlank(message = "Criterion description is required")
    @Min(value = 0, message = "Weight must be non-negative")
    private Double weight;
}
