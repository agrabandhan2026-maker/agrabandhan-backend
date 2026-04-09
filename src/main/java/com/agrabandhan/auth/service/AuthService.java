package com.agrabandhan.auth.service;

import com.agrabandhan.auth.dto.AuthDto;
import com.agrabandhan.auth.entity.User;
import com.agrabandhan.auth.repository.UserRepository;
import com.agrabandhan.auth.security.JwtTokenProvider;
import com.agrabandhan.common.exception.BadRequestException;
import com.agrabandhan.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    // In-memory OTP store for dev/MVP. Replace with Redis + Firebase Phone Auth in production.
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    private static final String DEV_OTP = "123456";
    private static final int OTP_EXPIRY_MINUTES = 5;
    private static final int MAX_OTP_ATTEMPTS = 5;

    public void sendOtp(AuthDto.SendOtpRequest request) {
        String phone = request.getPhoneNumber();

        // Rate limiting check
        OtpEntry existing = otpStore.get(phone);
        if (existing != null && existing.attempts >= MAX_OTP_ATTEMPTS
                && existing.createdAt.plusMinutes(30).isAfter(LocalDateTime.now())) {
            throw new BadRequestException("Too many OTP requests. Please try after 30 minutes.");
        }

        // Generate OTP (dev mode: fixed OTP; production: integrate Firebase Phone Auth)
        String otp = DEV_OTP; // TODO: Replace with real OTP generation + SMS dispatch

        otpStore.put(phone, new OtpEntry(otp, LocalDateTime.now(), 0));
        log.info("OTP generated for phone: {} (dev mode: {})", phone, otp);
    }

    @Transactional
    public AuthDto.AuthResponse verifyOtp(AuthDto.VerifyOtpRequest request) {
        String phone = request.getPhoneNumber();
        String otp = request.getOtp();

        // Validate OTP
        OtpEntry entry = otpStore.get(phone);
        if (entry == null) {
            throw new BadRequestException("No OTP requested for this number. Please request OTP first.");
        }

        if (entry.createdAt.plusMinutes(OTP_EXPIRY_MINUTES).isBefore(LocalDateTime.now())) {
            otpStore.remove(phone);
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        entry.attempts++;
        if (!entry.otp.equals(otp)) {
            if (entry.attempts >= MAX_OTP_ATTEMPTS) {
                otpStore.remove(phone);
                throw new BadRequestException("Maximum OTP attempts exceeded. Please request a new OTP.");
            }
            throw new BadRequestException("Invalid OTP. " + (MAX_OTP_ATTEMPTS - entry.attempts) + " attempts remaining.");
        }

        // OTP valid — clear it
        otpStore.remove(phone);

        // Find or create user
        boolean isNewUser = false;
        User user = userRepository.findByPhoneNumber(phone).orElse(null);

        if (user == null) {
            isNewUser = true;
            user = User.builder()
                    .phoneNumber(phone)
                    .build();
        }

        user.setLastLoginAt(LocalDateTime.now());

        if (request.getFcmToken() != null) {
            user.setFcmToken(request.getFcmToken());
        }

        // Generate tokens
        String accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        user.setRefreshToken(refreshToken);

        // Save (handles both insert and update)
        user = userRepository.save(user);

        // If new user, regenerate tokens with actual ID
        if (isNewUser) {
            accessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole().name());
            refreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
        }

        return AuthDto.AuthResponse.of(
                accessToken, refreshToken,
                user.getId(), user.getRole().name(),
                user.isProfileComplete(), isNewUser
        );
    }

    @Transactional
    public AuthDto.AuthResponse refreshToken(AuthDto.RefreshTokenRequest request) {
        String token = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(token)) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (!token.equals(user.getRefreshToken())) {
            throw new BadRequestException("Refresh token does not match. Please login again.");
        }

        if (!user.isActive()) {
            throw new BadRequestException("Account is suspended. Contact support.");
        }

        // Rotate tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(user.getId(), user.getRole().name());
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user.getId());
        user.setRefreshToken(newRefreshToken);
        userRepository.save(user);

        return AuthDto.AuthResponse.of(
                newAccessToken, newRefreshToken,
                user.getId(), user.getRole().name(),
                user.isProfileComplete(), false
        );
    }

    @Transactional
    public void logout(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        user.setRefreshToken(null);
        user.setFcmToken(null);
        userRepository.save(user);
    }

    // Inner class for OTP tracking
    private static class OtpEntry {
        String otp;
        LocalDateTime createdAt;
        int attempts;

        OtpEntry(String otp, LocalDateTime createdAt, int attempts) {
            this.otp = otp;
            this.createdAt = createdAt;
            this.attempts = attempts;
        }
    }
}
