package org.lucas.arbackend.dto.vr.session;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.util.ValidatedLabel;

@Data @Builder
@Schema(name = "VRSessionStartRequest", description = "Payload to initiate a new VR training session")
public class VRSessionStartRequest {

    @NotNull(message = "Student number is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The number of the student/trainee", example = "STU-123456789")
    private String studentNumber;

    @NotNull(message = "Course ID is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The course ID", example = "101", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long courseId;

    @NotNull(message = "Chapter ID is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The chapter ID", example = "100", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long chapterId;

    @NotNull(message = "Section ID is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The chapter section ID containing the VR scene", example = "28", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sectionId;

    @Schema(description = "Device identifier (headset serial or UUID)", example = "Oculus-Quest3-A1B2C3D4")
    private String deviceId;

    @NotNull(message = "The VR scene version ID is required")
    @Schema(description = "The VR scene version id", example = "42", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sceneVersionId;

    @Schema(description = "Headset model name", example = "Quest 3")
    private String headsetModel;
}
