package org.lucas.arbackend.dto.payfast;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.ZonedDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayFastSubscriptionDto {

    private Integer amount;
    private Integer cycles;

    @JsonProperty("cycles_complete")
    private Integer cyclesComplete;

    private Integer frequency;

    @JsonProperty("run_date")
    private ZonedDateTime runDate;

    private Integer status;

    @JsonProperty("status_text")
    private String statusText;

    private String token;

}
