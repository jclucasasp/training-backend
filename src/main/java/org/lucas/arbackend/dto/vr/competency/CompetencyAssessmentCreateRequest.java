package org.lucas.arbackend.dto.vr.competency;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lucas.arbackend.entity.vr.competency.embedded.CriterionAssessmentResult;

import java.util.List;

@Data @Builder
@NoArgsConstructor @AllArgsConstructor
public class CompetencyAssessmentCreateRequest {
    @Schema(description = "The id of the current session", example = "1")
    @NotNull(message = "Session id is required")
    private Long sessionId;

    @Schema(description = "The final score of the competency assessment", example = "100")
    @NotNull(message = "Competency assessment score is required")
    private Double score;

    @Schema(description = "Whether the competency assessment was passed or not", example = "true")
    @NotNull(message = "Competency assessment pass status is required")
    private Boolean passed;

    private Long startEventId;
    private Long endEventId;

    private List<CriterionAssessmentResult> criteriaResults;
}
