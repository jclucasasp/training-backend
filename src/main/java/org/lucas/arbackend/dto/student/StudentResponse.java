package org.lucas.arbackend.dto.student;

import lombok.Builder;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data @Builder
public class StudentResponse implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String studentNumber;
    private String firstName;
    private String lastName;
}
