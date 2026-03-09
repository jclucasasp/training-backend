package org.lucas.arbackend.dto.quiz;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter @Setter
public class QuizSubmissionRequest {
   private List<AnswerDTO> answers;
}
