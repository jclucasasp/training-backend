package org.lucas.arbackend.dto.student.helper;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class StudentRequest {

    @NotNull(message = "Organisation ID is required")
    private Long organisationId;

    @NotNull(message = "Student number is required")
    private String studentNumber;

    private Long apiKeyId;
}
