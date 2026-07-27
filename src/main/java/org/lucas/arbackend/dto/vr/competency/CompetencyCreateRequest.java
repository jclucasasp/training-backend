package org.lucas.arbackend.dto.vr.competency;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder
@AllArgsConstructor @NoArgsConstructor
public class CompetencyCreateRequest {
    @Schema(description = "The name of the competency", example = "Fire fighting")
    @NotBlank(message = "Competency name is required")
    private String name;

    @Schema(description = "The description of the competency", example = "How to safely put out a fire")
    @NotBlank(message = "Competency description is required")
    private String description;

    @Schema(description = "The associated scene for the competency", example = "21110")
    @NotNull(message = "Must include the associated scene id")
    private Long associatedSceneId;
}
