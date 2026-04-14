package com.agrabandhan.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class CommunityDto {

    // =============================================
    //  Verification
    // =============================================
    @Data
    public static class VerifyIdRequest {
        @NotNull private String documentType; // AADHAAR, PAN, VOTER_ID, PASSPORT, DRIVING_LICENSE
    }

    @Data
    public static class ReviewVerificationRequest {
        @NotNull private String status; // APPROVED or REJECTED
        private String rejectionReason;
    }

    @Data @Builder
    public static class VerificationResponse {
        private Long id;
        private String documentType;
        private String documentUrl;
        private String selfieUrl;
        private String status;
        private String rejectionReason;
        private LocalDateTime createdAt;
        private LocalDateTime reviewedAt;
    }

    // =============================================
    //  Endorsements
    // =============================================
    @Data
    public static class EndorsementRequest {
        @NotBlank private String endorsementText;
        private String relationship;
    }

    @Data @Builder
    public static class EndorsementResponse {
        private Long id;
        private Long endorserProfileId;
        private String endorserName;
        private String endorserGotra;
        private String endorsementText;
        private String relationship;
        private boolean approved;
        private LocalDateTime createdAt;
    }

    // =============================================
    //  Trust Score
    // =============================================
    @Data @Builder
    public static class TrustScoreResponse {
        private Integer totalScore;
        private Integer idVerificationScore;
        private Integer photoVerificationScore;
        private Integer endorsementScore;
        private Integer sabhaVerificationScore;
        private Integer completenessScore;
        private Integer responseRateScore;
        private List<String> badges;
        private LocalDateTime updatedAt;
    }

    // =============================================
    //  Samaj Sabha
    // =============================================
    @Data
    public static class CreateSabhaRequest {
        @NotBlank private String name;
        @NotBlank private String city;
        @NotBlank private String state;
        private String address;
        private String contactPerson;
        private String contactPhone;
        private String contactEmail;
    }

    @Data @Builder
    public static class SabhaResponse {
        private Long id;
        private String name;
        private String city;
        private String state;
        private String address;
        private String contactPerson;
        private String contactPhone;
        private Integer memberCount;
        private boolean myMembership;
        private boolean verifiedByLeader;
    }

    // =============================================
    //  Mutual Connections
    // =============================================
    @Data @Builder
    public static class MutualConnectionResponse {
        private int mutualSabhaCount;
        private List<String> mutualSabhaNames;
        private int mutualEndorsers;
    }
}
