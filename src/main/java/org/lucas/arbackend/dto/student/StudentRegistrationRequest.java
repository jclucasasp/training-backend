package org.lucas.arbackend.dto.student;


import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component // Standard for production to allow @Autowired
@Data @Builder
public class StudentRegistrationRequest {
    @NotNull(message = "Student email is required")
    private String studentNumber;

    @NotNull(message = "Student email is required")
    private String firstName;

    @NotNull(message = "Student email is required")
    private String lastName;
}
