package org.lucas.arbackend.dto.student;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data @Builder
public class StudentEnrollRequest {

    @NotNull(message = "Student number is required")
    private String studentNumber;

    @NotNull(message = "First name is required")
    private String firstName;

    @NotNull(message = "Last name is required")
    private String lastName;

    @NotNull(message = "API Key is required")
    private String hashedKey;
}

