package com.agrabandhan.admin.dto;

import com.agrabandhan.auth.entity.User;
import com.agrabandhan.profile.entity.Profile;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

public class AdminDto {

    @Data
    @Builder
    public static class DashboardStats {
        private long totalUsers;
        private long activeToday;
        private long newThisWeek;
        private long totalMatches;
        private long interestsSent;
        private long interestsAccepted;
    }

    @Data
    @Builder
    public static class UserResponse {
        private Long id;
        private String phoneNumber;
        private String role;
        private boolean active;
        private boolean profileComplete;
        private LocalDateTime lastLoginAt;
        private LocalDateTime createdAt;

        public static UserResponse from(User user) {
            return UserResponse.builder()
                    .id(user.getId())
                    .phoneNumber(user.getPhoneNumber())
                    .role(user.getRole().name())
                    .active(user.isActive())
                    .profileComplete(user.isProfileComplete())
                    .lastLoginAt(user.getLastLoginAt())
                    .createdAt(user.getCreatedAt())
                    .build();
        }
    }

    @Data
    @Builder
    public static class UserDetailResponse {
        private Long userId;
        private String phoneNumber;
        private String role;
        private boolean active;
        private boolean profileComplete;
        private LocalDateTime lastLoginAt;
        private LocalDateTime createdAt;

        // Profile summary (if exists)
        private String firstName;
        private String lastName;
        private String gender;
        private String gotra;
        private String currentCity;
        private Integer completenessScore;

        public static UserDetailResponse from(User user, Profile profile) {
            var builder = UserDetailResponse.builder()
                    .userId(user.getId())
                    .phoneNumber(user.getPhoneNumber())
                    .role(user.getRole().name())
                    .active(user.isActive())
                    .profileComplete(user.isProfileComplete())
                    .lastLoginAt(user.getLastLoginAt())
                    .createdAt(user.getCreatedAt());

            if (profile != null) {
                builder.firstName(profile.getFirstName())
                        .lastName(profile.getLastName())
                        .gender(profile.getGender() != null ? profile.getGender().name() : null)
                        .gotra(profile.getGotra() != null ? profile.getGotra().getDisplayName() : null)
                        .currentCity(profile.getCurrentCity())
                        .completenessScore(profile.getCompletenessScore());
            }

            return builder.build();
        }
    }

    @Data
    public static class RoleUpdateRequest {
        private String role;
    }
}
