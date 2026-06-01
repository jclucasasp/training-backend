package org.lucas.arbackend.dto.QAndA;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import org.lucas.arbackend.util.ValidatedLabel;

@Getter
@Schema(name = "ReplyRequest", description = "Payload layout required to post a reply comment or answer to an existing question thread")
public class ReplyRequest {

    @NotBlank(message = "Reply text body cannot be empty", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The text copy or solution response to the forum question", example = "Make sure you updated the route tables on BOTH VPCs, not just the initiator VPC.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String body;

    @Schema(description = "Administrative check flag allowing staff members to approve and pin this answer as the resolution to the thread", example = "false")
    private boolean isAcceptedAnswer = false;
}