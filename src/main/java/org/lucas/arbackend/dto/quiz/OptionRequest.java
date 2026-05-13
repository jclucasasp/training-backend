package org.lucas.arbackend.dto.quiz;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class OptionRequest {
    @NotBlank(message = "Param 'text' for the question is missing or blank")
    private String text;

    @NotNull(message = "Param 'correct' should be true or false")
    private boolean correct;
}
