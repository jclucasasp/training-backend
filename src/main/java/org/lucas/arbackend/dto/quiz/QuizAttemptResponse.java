package org.lucas.arbackend.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter @Setter @Builder
@Schema(name = "QuizAttemptResponse", description = "Output data summary sheet profiling historical runtime transaction details tracking individual student execution performance records")
public class QuizAttemptResponse {

    @Schema(description = "Unique database entry tracking this individual sheet instance transaction execution", example = "101")
    private Long attemptId;

    @Schema(description = "The unique primary structural verification token mapping back to the target evaluation context module template", example = "55")
    private Long quizId;

    @Schema(description = "The final calculated precision accuracy score milestone earned across this evaluation sequence", example = "85.00")
    private BigDecimal score;

    @Schema(description = "Systemic indicator marking whether the calculated precision grade scales passed minimum testing thresholds", example = "true")
    private boolean isPassed;

    @Schema(description = "ISO date-time checkpoint record locking the precise temporal event timestamp tracking execution completion", example = "2026-10-27T10:15:30")
    private LocalDateTime completedAt;

    @Schema(description = "The raw or fully parsed serialization tree charting student choices selections array layouts. Typically omitted from basic listing operations.",
            example = "[{\"questionId\": 250, \"selectedOptionId\": 4001}]")
    private Object answers;
}