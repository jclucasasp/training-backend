package org.lucas.arbackend.dto.vr.competency;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Builder
public record CompetencyResponse(
        Long id,
        @Schema(description = "The name of the competency assessment", example = "Fire Safety")
        String name,
        @Schema(description = "The description of the competency assessment",
                example = "This competency assessment is designed to test the user's knowledge of fire safety.")
        String description,
        @Schema(description = "The id of the associated scene", example = "1")
        Long associatedSceneId,
        @Schema(description = "The different criteria associated with the competency assessment")
        List<CompetencyCriterionResponse> criteria,
        @Schema(description = "The date and time the competency assessment was created", example = "2023-01-01T00:00:00")
        LocalDateTime  createdAt
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
