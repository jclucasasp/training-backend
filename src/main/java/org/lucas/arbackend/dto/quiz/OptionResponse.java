package org.lucas.arbackend.dto.quiz;

import io.swagger.v3.oas.annotations.media.Schema;
import java.io.Serial;
import java.io.Serializable;

@Schema(name = "OptionResponse", description = "The structural response mapping an individual quiz choice option")
public record OptionResponse(

        @Schema(description = "Unique internal database primary sequence index locating this option", example = "4001")
        Long id,

        @Schema(description = "The display text for the answer selection choice", example = "Virtual Private Cloud (VPC)")
        String text,

        @Schema(description = "Flags whether this answer is correct. Note: Secure your endpoints to omit this property from students during active exam sessions if necessary.", example = "true")
        boolean correct

) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}