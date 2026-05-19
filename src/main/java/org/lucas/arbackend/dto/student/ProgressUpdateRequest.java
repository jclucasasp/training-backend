package org.lucas.arbackend.dto.student;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ProgressUpdateRequest {

    @NotNull(message = "Param 'chapterId' missing or blank")
    private Long chapterId;

    @NotNull(message = "Param 'sectionId' missing or blank")
    private Long sectionId;

    @NotNull(message = "Param 'isCompleted' missing or blank")
    private Boolean isCompleted;
}
