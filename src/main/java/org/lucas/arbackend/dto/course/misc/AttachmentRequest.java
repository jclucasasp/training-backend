package org.lucas.arbackend.dto.course.misc;

import jakarta.validation.constraints.NotNull;
import org.lucas.arbackend.util.ValidatedLabel;

public class AttachmentRequest {
    @NotNull(message = "Attachment name is required", groups = ValidatedLabel.OnCreate.class)
    private String name;

    @NotNull(message = "Attachment url is required", groups = ValidatedLabel.OnCreate.class)
    private String url;
}
