package org.lucas.arbackend.dto.payfast;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "PayFastSubUpdateReqDto", description = "Payload layout supplied to request modifications to an active PayFast recurring subscription mandate")
public class PayFastSubUpdateReqDto {

    @JsonProperty("amount")
    @Schema(description = "The adjusted billing rate price change value represented in normal decimal format", example = "200.00")
    private Double priceUpdate;

    @JsonProperty("frequency")
    @Schema(description = "The updated billing gap interval frequency lookup code (e.g., 3 = Monthly, 6 = Annual)", example = "3")
    private Integer frequencyUpdate;

    @JsonProperty("cycles")
    @Schema(description = "The updated overall runtime limit of targeted billing intervals. Pass 0 for ongoing billing lifecycle.", example = "12")
    private Integer cyclesUpdate;

    @JsonProperty("run_date")
    @Schema(description = "The explicitly scheduled date targeting when the updated subscription tier specifications take effect", example = "2026-07-01")
    private LocalDate dateUpdate;
}