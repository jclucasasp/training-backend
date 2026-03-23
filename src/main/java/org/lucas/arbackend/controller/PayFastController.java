package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.lucas.arbackend.exception.ErrorDetailsResponse;
import org.lucas.arbackend.service.payment.PayFastSubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
public class PayFastController {

    private final PayFastSubscriptionService subscriptionService;

    /**
     * PayFast ITN (Instant Transaction Notification) handler.
     * * IMPORTANT: We use @RequestParam Map<String, String> because Spring
     * populates this with a LinkedHashMap, preserving the order of
     * parameters as sent by PayFast's servers.
     */
    @PostMapping(value = "/itn", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @Operation(summary = "PayFast Instant Transaction Notification")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Notification received and processed"),
            @ApiResponse(responseCode = "401", description = "Security violation",
            content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class))),
            @ApiResponse(responseCode = "500", description = "Internal Server Error",
                    content = @Content(schema = @Schema(implementation = ErrorDetailsResponse.class)))
    })
    // TODO: Move the logic out to the service class
    public ResponseEntity<String> handlePayFastItn(HttpServletRequest request) {
        Map<String, String> params = new LinkedHashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            String paramValue = request.getParameter(paramName);
            params.put(paramName, paramValue != null ? paramValue : "");
        }
        try {
            // Log the custom_int1 (Org ID) for tracking
            log.info("Received PayFast ITN notification for Organisation ID: {}", params.get("custom_int1"));

            // Pass the map directly to your Service
            subscriptionService.processIpn(params);

            // PayFast MUST receive an 'OK' or 200 response to acknowledge the notification
            return ResponseEntity.ok("OK");

        } catch (SecurityException e) {
            log.warn("PayFast security violation: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            log.error("Error processing PayFast ITN", e);
            // Return 500 so PayFast knows to retry later
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/subscriptions/{token}")
    public ResponseEntity<String> getSubscriptionStatus(@PathVariable String token) {
        try {
            String status = subscriptionService.fetchSubscriptionStatus(token);
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            log.error("Failed to fetch subscription status for token: {}", token, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}