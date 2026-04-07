package org.lucas.arbackend.dto.payfast;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PayFastSubUpdateReqDto {
    @JsonProperty("amount")
    private Double priceUpdate;
    @JsonProperty("frequency")
    private Integer frequencyUpdate;
    @JsonProperty("cycles")
    private Integer cyclesUpdate;
    @JsonProperty("run_date")
    private LocalDate dateUpdate;
}
