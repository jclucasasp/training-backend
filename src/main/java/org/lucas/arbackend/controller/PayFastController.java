package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.dto.payfast.PayFastSubUpdateResDto;
import org.lucas.arbackend.dto.payfast.PayFastSubscriptionDto;
import org.lucas.arbackend.dto.payfast.PayFastSubUpdateReqDto;
import org.lucas.arbackend.exception.ErrorDetailsResponse;
import org.lucas.arbackend.service.payment.PayFastSubscriptionService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @Operation(summary = "Fetch Subscription PaymentStatus",
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
    public ResponseEntity<List<PayFastSubscriptionDto>> getSubscriptionStatus() {
        return ResponseEntity.ok(subscriptionService.fetchSubscriptionStatus());
    }

    @PatchMapping("/subscriptions/update")
      @Operation(
        summary = "Update Recurring Subscription",
        description = "Modifies an existing PayFast subscription. Supports decreasing the amount, changing frequency (3 for monthly, 6 for yearly), adjusting cycles, or updating the next run date."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Subscription updated successfully",
                    content = @Content(mediaType = "application/json",
                    schema = @Schema(implementation = PayFastSubscriptionDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid update parameters provided",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "401", description = "Signature mismatch or unauthorized request",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existing subscription found for this organization",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    public ResponseEntity<PayFastSubUpdateResDto> updateSubscription(@RequestBody @Validated PayFastSubUpdateReqDto subscriptionDto) {
        return ResponseEntity.ok(subscriptionService.updateSubscription(subscriptionDto));
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