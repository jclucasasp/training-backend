package org.lucas.arbackend.dto.course.attachment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.lucas.arbackend.util.ValidatedLabel;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(name = "AttachmentRequest", description = "Payload schema used to attach extra reference files or assets to a lesson module")
public class AttachmentRequest {
    @Schema(description = "Optional unique identifier for modifying an existing attachment resource record", example = "12")
    private Long id;

    @NotBlank(message = "File name is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The display name of the resource file", example = "Subnetting_Cheat_Sheet.pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileName;

    @NotBlank(message = "File type description is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The explicit MIME type classification extension format", example = "application/pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileType;

    @NotBlank(message = "File download link URL is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The fully qualified CDN download link location tracking the asset target", example = "https://cdn.example.com/courses/attachments/subnet-cheat.pdf", requiredMode = Schema.RequiredMode.REQUIRED)
    private String fileUrl;
}
