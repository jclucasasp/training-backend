package org.lucas.arbackend.dto.vr.scene;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;

@Builder
@Schema(name = "VRSceneVersionResponse", description = "The version of the scene")
public record VRSceneVersionResponse(
        Long id,
        @Schema(description = "The id of the referenced scene")
        Long sceneId,
        @Schema(description = "The version of the scene", example = "1.0.0")
        String versionTag,
        @Schema(description = "The active changes to the scene", example = "Fixed bugs and improved user experience")
        String changeLog,
        @Schema(description = "A flag describing whether the version is active or not", example = "true")
        Boolean isActive,
        @Schema(description = "The URL of the environmental file", example = "https://example.com/environmental_file")
        String environmentalFileUrl,
        @Schema(description = "The JSON representation of the scene hierarchy",
                example = "{\"entities\": [{\"id\": \"valve_01\", \"type\": \"valve\", \"position\": [1.2, 0.8, -0.4]}]}")
        String hierarchyJson
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
