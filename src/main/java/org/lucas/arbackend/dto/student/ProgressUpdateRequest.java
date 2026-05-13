package org.lucas.arbackend.dto.student;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ProgressUpdateRequest {

    @NotNull(message = "Param 'enrollmentId' missing or blank")
    private Long enrollmentId;

    @NotNull(message = "Param 'sectionId' missing or blank")
    private Long sectionId;
}
