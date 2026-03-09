package org.lucas.arbackend.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder
public class QuizAttemptResponse {

    @Schema(example = "101")
    private Long attemptId;

    @Schema(example = "55")
    private Long quizId;

    @Schema(example = "85.00")
    private BigDecimal score;

    @Schema(example = "true")
    private boolean isPassed;

    @Schema(example = "2023-10-27T10:15:30")
    private LocalDateTime completedAt;

    @Schema(description = "The raw or parsed JSON of student answers. Usually null in list views.",
            example = "[{\"questionId\": 1, \"selectedOptionId\": 4}]")
    private Object answers;

}
