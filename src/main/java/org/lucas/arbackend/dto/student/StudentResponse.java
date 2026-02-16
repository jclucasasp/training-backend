package org.lucas.arbackend.dto.student;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data @Builder
public class StudentResponse implements Serializable {
    private Long id;
    private String studentNumber;
    private String firstName;
    private String lastName;
}
