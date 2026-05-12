package org.lucas.arbackend.dto.course.attachment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.lucas.arbackend.util.ValidatedLabel;
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttachmentRequest {

    private Long id;

    @NotBlank(message = "Param 'fileName' missing or blank", groups = ValidatedLabel.OnCreate.class)
    private String fileName;

    @NotBlank(message = "Param 'fileType' missing or blank", groups = ValidatedLabel.OnCreate.class)
    private String fileType;

    @NotBlank(message = "Param 'fileUrl' missing or blank", groups = ValidatedLabel.OnCreate.class)
    private String fileUrl;
}
