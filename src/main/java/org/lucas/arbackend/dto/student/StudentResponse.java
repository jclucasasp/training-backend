package org.lucas.arbackend.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.lucas.arbackend.entity.quiz.StudentQuiz;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Schema(name = "StudentResponse", description = "The structural endpoint payload returning fully mapped multi-tenant student identities alongside aggregated testing profiles records")
public record StudentResponse (

        @Schema(description = "Unique auto-generated internal database sequencing primary index identifier link", example = "1024")
        Long id,

        @Schema(description = "The unique institutional registration identity tracking code assigned to the student", example = "STU20268841")
        String studentNumber,

        @Schema(description = "The given name of the student", example = "Lucas")
        String firstName,

        @Schema(description = "The family name or surname of the student", example = "Devan")
        String lastName,

        @Schema(description = "The unique internal database identifier referencing the parent organization tenant registry", example = "104")
        Long organisation,

        @Schema(description = "A collection array containing historical quiz iteration assessment attempts metadata associated with this student's learning history profile")
        List<StudentQuizResponse> studentQuizzes

) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}