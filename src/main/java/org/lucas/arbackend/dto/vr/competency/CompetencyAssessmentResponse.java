package org.lucas.arbackend.dto.vr.competency;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.lucas.arbackend.entity.vr.competency.AssessedBy;
import org.lucas.arbackend.entity.vr.competency.embedded.CriterionAssessmentResult;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CompetencyAssessmentResponse(
        Long id,
        @Schema(description = "The session id for the competency assessment", example = "1")
        Long sessionId,
        @Schema(description = "The number of the student taking the assessment", example = "STU-2000345")
        String studentNumber,
        @Schema(description = "The id of the competency being assessed", example = "1")
        Long competencyId,
        @Schema(description = "The final score of the competency assessment", example = "100")
        Double score,
        @Schema(description = "Whether the competency assessment was passed or not", example = "true")
        Boolean passed,
        @Schema(description = "The system used for grading the assessment", example = "AI, INSTRUCTOR, SYSTEM")
        AssessedBy assessedBy,
        @Schema(description = "The id of the event that started the competency assessment", example = "1")
        Long startEventId,
        @Schema(description = "The results of the different assessments")
        List<CriterionAssessmentResult> criteriaResults,
        @Schema(description = "The date and time when the assessment was marked", example = "2023-01-01T12:00:00Z")
        LocalDateTime assessedAt
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
