package org.lucas.arbackend.dto.student;

import lombok.Builder;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.quiz.StudentQuiz;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Builder
public record StudentResponse (
        Long id,
        String studentNumber,
        String firstName,
        String lastName,
        Organisation organisation,
        List<StudentQuiz> studentQuizzes
) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}
