package org.lucas.arbackend.dto.course.misc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.lucas.arbackend.util.ValidatedLabel;
@Data
public class AttachmentRequest {

    private Long id;

    @NotNull(message = "Attachment name is required", groups = ValidatedLabel.OnCreate.class)
    private String name;

    @NotNull(message = "Attachment url is required", groups = ValidatedLabel.OnCreate.class)
    private String url;
}
