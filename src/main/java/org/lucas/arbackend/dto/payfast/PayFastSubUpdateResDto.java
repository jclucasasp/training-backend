package org.lucas.arbackend.dto.payfast;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@Schema(name = "PayFastSubUpdateResDto", description = "The structural output schema returned confirming successfully executed subscription modifications inside PayFast systems")
public class PayFastSubUpdateResDto {

    @Schema(description = "PayFast response processing identifier code status", example = "200")
    private Integer code;

    @Schema(description = "Operational confirmation status summary label text", example = "success")
    private String status;

    @Schema(description = "The data wrapper containing the confirmed updated registration detail states")
    private DataWrapper data;

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    @Schema(name = "PayFastSubUpdateDataWrapper", description = "Wrapper block detailing altered parameters properties")
    public static class DataWrapper {
        private ResponseDetails response;
    }

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    @Schema(name = "PayFastSubUpdateResponseDetails", description = "The verified values actively applied to the PayFast account ledger instance")
    public static class ResponseDetails {

        @Schema(description = "The unique alphanumeric transaction key identifying this secure credit card mandate reference profile", example = "abc123de-456f-7890-ghij-klmnopqrstuv")
        private String token;

        @Schema(description = "The newly applied billing rate value confirmed by the gateway in South African Cents", example = "20000.00")
        private Double amount; // PayFast returns amounts in cents (e.g., 20000 = R200.00)

        @Schema(description = "The updated total billing iteration limit boundaries tracking structural contract parameters", example = "12")
        private Integer cycles;

        @Schema(description = "The newly verified billing interval gap returned as a string literal classification token code", example = "3")
        private String frequency; // Frequency is usually returned as a String "6" or "3"

        @JsonProperty("status")
        @Schema(description = "The newly confirmed runtime state marker text", example = "Active")
        private String subscriptionStatus;

        @JsonProperty("run_date")
        @Schema(description = "The verified processing milestone date tracking when the next transaction executes under new price rules", example = "2026-07-01")
        private LocalDate runDate;
    }
}