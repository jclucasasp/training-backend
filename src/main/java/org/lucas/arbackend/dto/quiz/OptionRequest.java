package org.lucas.arbackend.dto.quiz;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter @Builder
public class OptionRequest {

    private String text;
    private boolean correct;
}
