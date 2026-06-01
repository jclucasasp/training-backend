package org.lucas.arbackend.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data @Builder
@Schema(name = "ProgressUpdateRequest", description = "Payload layout provided to record a student progression milestone after completing a structured lesson module section")
public class ProgressUpdateRequest {

    @NotNull(message = "Chapter identifier is required")
    @Schema(description = "The unique internal primary tracking index pinpointing the parent course chapter node template context", example = "45", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long chapterId;

    @NotNull(message = "Section identifier is required")
    @Schema(description = "The unique internal primary tracking index pinpointing the specific lesson section milestone completed", example = "28", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long sectionId;
}