package org.lucas.arbackend.dto.vr.scene;

import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;

@Builder
public record VRSceneResolutionResponse(
        Long sectionId,
        Long sceneId,
        Long sceneVersionId,
        String versionTag,
        String environmentalFileUrl,
        String hierarchyJson
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
