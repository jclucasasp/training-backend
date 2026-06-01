package org.lucas.arbackend.dto.course.attachment;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serial;
import java.io.Serializable;

@Schema(name = "AttachmentResponse", description = "The structural output payload returned when viewing supplementary assets bound to a course section node")
public record AttachmentResponse(
        @Schema(description = "Unique resource asset index id location", example = "12")
        Long id,

        @Schema(description = "The customized display file name string", example = "Subnetting_Cheat_Sheet.pdf")
        String fileName,

        @Schema(description = "The explicit application MIME classification tag format string", example = "application/pdf")
        String fileType,

        @Schema(description = "The fully accessible public destination address address stream location", example = "https://cdn.example.com/courses/attachments/subnet-cheat.pdf")
        String fileUrl
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
