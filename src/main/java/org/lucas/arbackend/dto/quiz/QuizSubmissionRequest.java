package org.lucas.arbackend.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter @Setter
@Schema(name = "QuizSubmissionRequest", description = "Payload wrapper containing a collection of user-selected answers submitted for quiz verification evaluation processing")
public class QuizSubmissionRequest {

   @NotEmpty(message = "Submission must contain at least one answered question selection")
   @Valid
   @Schema(description = "The comprehensive distinct collection of questions paired with their chosen answer options parameters", requiredMode = Schema.RequiredMode.REQUIRED)
   private Set<AnswerDTO> answers;
}