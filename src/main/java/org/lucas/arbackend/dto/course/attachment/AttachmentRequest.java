package org.lucas.arbackend.dto.course.attachment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.lucas.arbackend.util.ValidatedLabel;
@Data
public class AttachmentRequest {

    private Long id;

    @NotNull(message = "Attachment fileName is required", groups = ValidatedLabel.OnCreate.class)
    private String fileName;

    @NotBlank(message = "Attachment fileType is required", groups = ValidatedLabel.OnCreate.class)
    private String fileType;

    @NotNull(message = "Attachment fileUrl is required", groups = ValidatedLabel.OnCreate.class)
    private String fileUrl;
}
