package com.agrabandhan.auth.controller;

import com.agrabandhan.auth.dto.AuthDto;
import com.agrabandhan.auth.entity.User;
import com.agrabandhan.auth.service.AuthService;
import com.agrabandhan.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "OTP-based authentication APIs")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/send-otp")
    @Operation(summary = "Send OTP to phone number")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody AuthDto.SendOtpRequest request) {
        authService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.success("OTP sent successfully"));
    }

    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP and get JWT tokens")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> verifyOtp(
            @Valid @RequestBody AuthDto.VerifyOtpRequest request) {
        AuthDto.AuthResponse response = authService.verifyOtp(request);
        return ResponseEntity.ok(ApiResponse.success("Authentication successful", response));
    }

    @PostMapping("/refresh-token")
    @Operation(summary = "Refresh access token using refresh token")
    public ResponseEntity<ApiResponse<AuthDto.AuthResponse>> refreshToken(
            @Valid @RequestBody AuthDto.RefreshTokenRequest request) {
        AuthDto.AuthResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success("Token refreshed", response));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and invalidate tokens")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal User user) {
        authService.logout(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }
}
