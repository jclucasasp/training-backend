package org.lucas.arbackend.dto.QAndA;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.lucas.arbackend.util.ValidatedLabel;

import java.io.Serial;
import java.io.Serializable;

@Getter
public class ReplyRequest {
    @NotBlank(message = "Body is required", groups = ValidatedLabel.OnCreate.class)
    private String body;

    private boolean isAcceptedAnswer = false;
}
