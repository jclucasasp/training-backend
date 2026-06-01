package org.lucas.arbackend.dto.payfast;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.ZonedDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(name = "PayFastSubscriptionDto", description = "Response payload representing the full subscription detail state retrieved from PayFast")
public class PayFastSubscriptionDto {

    @Schema(description = "PayFast API response status code (e.g., 200 for success)", example = "200")
    private int code;

    @Schema(description = "The text summary status of the API operation request", example = "success")
    private String status;

    @Schema(description = "The core data payload object containing the subscription records")
    private DataPayload data;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(name = "PayFastDataPayload", description = "Wrapper object enclosing the direct response payload details")
    public static class DataPayload {
        private SubscriptionDetails response;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    @Schema(name = "PayFastSubscriptionDetails", description = "Granular subscription profile state parameters registered on PayFast")
    public static class SubscriptionDetails {

        @Schema(description = "The recurring subscription debit amount calculated strictly in South African Cents (ZAR Cents)", example = "169900")
        private int amount; // Note: This is in cents (e.g., 169900 = R1699.00)

        @Schema(description = "The total number of payment cycles scheduled under this contract. 0 means infinite/ongoing.", example = "0")
        private int cycles;

        @JsonProperty("cycles_complete")
        @Schema(description = "The count of successfully collected billing iteration cycles", example = "3")
        private int cyclesComplete;

        @Schema(description = "The frequency type code indicating billing runtime gaps (e.g., 3 = Monthly, 6 = Annual)", example = "3")
        private int frequency;

        @JsonProperty("run_date")
        @Schema(description = "ISO zoned timestamp detailing when the next subscription charge iteration executes", example = "2026-06-15T00:00:00+02:00")
        private ZonedDateTime runDate;

        @Schema(description = "Numeric internal PayFast status marker code mapping current lifecycle state (e.g., 1 = Active)", example = "1")
        private int status;

        @JsonProperty("status_reason")
        @Schema(description = "The underlying description text outlining why a status change occurred (if applicable)", example = "Card updated successfully")
        private String statusReason;

        @JsonProperty("status_text")
        @Schema(description = "Human-readable label mapping the subscription lifecycle state", example = "Active")
        private String statusText;

        @Schema(description = "The unique token tracking this specific billing profile instance mapping secure credit card mandates", example = "abc123de-456f-7890-ghij-klmnopqrstuv")
        private String token;
    }
}