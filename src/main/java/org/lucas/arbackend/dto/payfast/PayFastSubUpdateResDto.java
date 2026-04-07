package org.lucas.arbackend.dto.payfast;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PayFastSubUpdateResDto {
     private Integer code;
    private String status;
    private DataWrapper data;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class DataWrapper {
        private ResponseDetails response;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ResponseDetails {
        private String token;

        // PayFast returns amounts in cents (e.g., 20000 = R200.00)
        private Double amount;

        private Integer cycles;

        // Frequency is usually returned as a String "6" or "3"
        private String frequency;

        @JsonProperty("status")
        private String subscriptionStatus;

        @JsonProperty("run_date")
        private LocalDate runDate;
    }
}
