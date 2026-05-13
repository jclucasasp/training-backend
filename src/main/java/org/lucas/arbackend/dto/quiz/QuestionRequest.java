package org.lucas.arbackend.dto.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter @Builder
public class QuestionRequest {
    @NotBlank(message = "Param 'text' for question is missing or blank")
    private String text;

    @NotBlank(message = "Param 'type' for question is missing or blank")
    private String type;

    @NotEmpty(message = "List 'options' for question is missing or empty")
    List<OptionRequest> options;
}
