package org.lucas.arbackend.dto.payfast;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.ZonedDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class PayFastSubscriptionDto {
private int code;
    private String status;
    private DataPayload data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class DataPayload {
        private SubscriptionDetails response;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SubscriptionDetails {
        private int amount; // Note: This is in cents (1699)
        private int cycles;

        @JsonProperty("cycles_complete")
        private int cyclesComplete;

        private int frequency;

        @JsonProperty("run_date")
        private ZonedDateTime runDate;

        private int status;

        @JsonProperty("status_reason")
        private String statusReason;

        @JsonProperty("status_text")
        private String statusText;

        private String token;
    }
}
