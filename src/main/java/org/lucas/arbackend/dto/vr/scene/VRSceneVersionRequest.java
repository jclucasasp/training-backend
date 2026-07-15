package org.lucas.arbackend.dto.vr.scene;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor @NoArgsConstructor
@Data @Builder
@Schema(name = "VRSceneVersionRequest", description = "The version of the scene")
public class VRSceneVersionRequest {
    @Schema(description = "The version of the scene", example = "1.0.0")
    private String versionTag;

    @Schema(description = "")
    private String changeLog;

    @Schema(description = "A flag describing whether the version is active or not", example = "true")
    private Boolean isActive;

    @Schema(description = "The URL of the environmental file", example = "https://example.com/environmental_file")
    private String environmentalFileUrl;

    @Schema(description = "The JSON representation of the scene hierarchy",
            example = "{\"entities\": [{\"id\": \"valve_01\", \"type\": \"valve\", \"position\": [1.2, 0.8, -0.4]}]}")
    private String hierarchyJson;
}
