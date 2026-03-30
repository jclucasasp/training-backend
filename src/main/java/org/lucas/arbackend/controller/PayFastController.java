package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.dto.payfast.PayFastSubscriptionDto;
import org.lucas.arbackend.exception.ErrorDetailsResponse;
import org.lucas.arbackend.service.payment.PayFastSubscriptionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PayFastController {

    private final PayFastSubscriptionService subscriptionService;

    @PostMapping(value = "/itn", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(summary = "PayFast Instant Transaction Notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification received and processed"),
            @ApiResponse(responseCode = "401", description = "Security violation",
            content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    public ResponseEntity<String> handlePayFastItn(HttpServletRequest request) {
            return ResponseEntity.ok(subscriptionService.processIpn(request));
    }

    @GetMapping("/subscriptions/fetch")
    @Operation(summary = "Fetch Subscription Status",
               description = "Queries the PayFast API to retrieve current billing cycles, amount, and status for a subscription token.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription details retrieved successfully",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PayFastSubscriptionDto.class))),
            @ApiResponse(responseCode = "404", description = "Subscription token not found",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    public ResponseEntity<PayFastSubscriptionDto> getSubscriptionStatus() {
        return ResponseEntity.ok(subscriptionService.fetchSubscriptionStatus());
    }

    @PutMapping("/subscriptions/cancel")
    @Operation(summary = "Cancel Subscription",
               description = "Sets a subscription status to cancelled on the PayFast gateway. Returns true if successful.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Cancellation request processed",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = Boolean.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or subscription already cancelled",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    public ResponseEntity<Boolean> cancelSubscription() {
        return ResponseEntity.ok(subscriptionService.cancelPayFastSubscription());
    }
}