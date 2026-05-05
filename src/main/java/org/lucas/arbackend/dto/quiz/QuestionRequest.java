package org.lucas.arbackend.dto.quiz;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter @Setter @Builder
public class QuestionRequest {
    private String text;
    private String type;
    List<OptionRequest> options;
}
