package org.lucas.arbackend.dto.student;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ProgressUpdateRequest {

    @NotNull(message = "Enrollment Id is required")
    private Long enrollmentId;

    @NotNull(message = "Section Id is required")
    private Long sectionId;
}
