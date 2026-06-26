package org.lucas.arbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.security.LoginRequest;
import org.lucas.arbackend.service.security.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "1. Auth", description = "Handle user login and sets the session in Redis")
public class AuthController {

    private final AuthenticationManager manager;
    private final AuthService authService;

    @Operation(summary = "User Login", description = "Authenticates user and starts a Redis session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<Void> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

        Authentication auth = manager.authenticate(token);
        SecurityContextHolder.getContext().setAuthentication(auth);

        // Create the session and save it in Redis
        HttpSession session = request.getSession(true);
        session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

        return ResponseEntity.ok().build();
    }


   @Operation(summary = "User Logout", description = "Invalidates the session and clears the context")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful")
    })
   @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "OTP Generation", description = "Generates an OTP and email it to the provided email if it exists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP generated and emailed"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/otp/{email}")
    public ResponseEntity<Void> otp(@PathVariable String email) {
        authService.sendOtp(email);
        return ResponseEntity.ok().build();
    }
     @Operation(summary = "OTP Generation", description = "Generates an OTP and email it to the provided email if it exists")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OTP generated and emailed"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
     @PostMapping("/reset-password/{otp}")
    public ResponseEntity<Void> passwordReset(@PathVariable String otp, @Validated @RequestBody LoginRequest loginRequest) {
        authService.changePassword(loginRequest.getEmail(), otp, loginRequest.getPassword());
        return ResponseEntity.ok().build();
     }

}
