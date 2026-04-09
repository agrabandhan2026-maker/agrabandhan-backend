package com.agrabandhan.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

public class AuthDto {

    @Data
    public static class SendOtpRequest {
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
        private String phoneNumber;
    }

    @Data
    public static class VerifyOtpRequest {
        @NotBlank(message = "Phone number is required")
        @Pattern(regexp = "^[6-9]\\d{9}$", message = "Invalid Indian mobile number")
        private String phoneNumber;

        @NotBlank(message = "OTP is required")
        @Pattern(regexp = "^\\d{6}$", message = "OTP must be 6 digits")
        private String otp;

        private String fcmToken;
    }

    @Data
    public static class RefreshTokenRequest {
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;
    }

    @Data
    public static class AuthResponse {
        private String accessToken;
        private String refreshToken;
        private Long userId;
        private String role;
        private boolean profileComplete;
        private boolean newUser;

        public static AuthResponse of(String accessToken, String refreshToken,
                                       Long userId, String role,
                                       boolean profileComplete, boolean newUser) {
            AuthResponse response = new AuthResponse();
            response.accessToken = accessToken;
            response.refreshToken = refreshToken;
            response.userId = userId;
            response.role = role;
            response.profileComplete = profileComplete;
            response.newUser = newUser;
            return response;
        }
    }
}
