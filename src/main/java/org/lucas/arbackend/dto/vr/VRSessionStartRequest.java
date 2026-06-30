package org.lucas.arbackend.dto.vr;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.util.ValidatedLabel;

@Data @Builder
@Schema(name = "VRSessionStartRequest", description = "Payload to initiate a new VR training session")
public class VRSessionStartRequest {

    @NotNull(message = "Section ID is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The chapter section ID containing the VR scene", example = "28", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sectionId;

    @Schema(description = "Device identifier (headset serial or UUID)", example = "Oculus-Quest3-A1B2C3D4")
    private String deviceId;

    @Schema(description = "Headset model name", example = "Quest 3")
    private String headsetModel;
}
