package org.lucas.arbackend.dto.quiz;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Getter @Setter
public class QuizSubmissionRequest {
   private Set<AnswerDTO> answers;
}
