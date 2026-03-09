package org.lucas.arbackend.dto.quiz;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AnswerDTO extends QuizSubmissionRequest {
    private Long questionId;
    private Long selectedOptionId;
}
