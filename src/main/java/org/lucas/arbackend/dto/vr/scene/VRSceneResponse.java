package org.lucas.arbackend.dto.vr.scene;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;

@Builder
@Schema(name = "VRSceneResponse", description = "VR scene response")
public record VRSceneResponse(
        Long id,
        @Schema(description = "The title of the scene", example = "Confined Space Entry Simulation")
        String title,
        @Schema(description = "The description of the scene", example = "Hazardous environment safety training")
        String description
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
