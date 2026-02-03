package org.lucas.arbackend.dto.student;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

@Component
@Data @Builder
public class StudentResponse {
    private Long id;
    private String studentNumber;
    private String firstName;
    private String lastName;
}
