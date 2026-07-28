package org.lucas.arbackend.dto.vr.competency;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Builder
public record StudentCompetencyDashboardResponse(
        @Schema(description = "The number of the student", example = "STU-2000345")
        String studentNumber,
        @Schema(description = "The number of competencies assessed", example = "10")
        Long totalCompetenciesAssessed,
        @Schema(description = "The number of competencies passed", example = "8")
        Long totalPassed,
        @Schema(description = "The number of competencies failed", example = "2")
        Double overallPassRatePercentage,
        @Schema(description = "The most recent competency assessments")
        List<CompetencyAssessmentResponse> recentAssessments
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
