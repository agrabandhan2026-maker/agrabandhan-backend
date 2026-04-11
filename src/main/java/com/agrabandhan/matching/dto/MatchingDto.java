package com.agrabandhan.matching.dto;

import com.agrabandhan.matching.entity.PartnerPreference.ManglikPreference;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

public class MatchingDto {

    // =============================================
    //  Partner Preferences
    // =============================================
    @Data
    public static class PartnerPreferenceRequest {
        @Min(18) @Max(70) private Integer ageMin;
        @Min(18) @Max(70) private Integer ageMax;
        private Integer heightMin;
        private Integer heightMax;
        private List<String> preferredMaritalStatuses;
        private String minQualification;
        private List<String> preferredEmployedIn;
        private String minIncomeRange;
        private List<String> preferredCities;
        private List<String> preferredStates;
        private String preferredCountry;
        private String preferredDiet;
        private Boolean smokingAcceptable;
        private Boolean drinkingAcceptable;
        private String preferredFamilyType;
        private List<String> excludeGotras;
        private ManglikPreference manglikPreference;
    }

    @Data
    @Builder
    public static class PartnerPreferenceResponse {
        private Long id;
        private Integer ageMin;
        private Integer ageMax;
        private Integer heightMin;
        private Integer heightMax;
        private List<String> preferredMaritalStatuses;
        private String minQualification;
        private List<String> preferredEmployedIn;
        private String minIncomeRange;
        private List<String> preferredCities;
        private List<String> preferredStates;
        private String preferredCountry;
        private String preferredDiet;
        private Boolean smokingAcceptable;
        private Boolean drinkingAcceptable;
        private String preferredFamilyType;
        private List<String> excludeGotras;
        private String manglikPreference;
    }

    // =============================================
    //  Daily Matches
    // =============================================
    @Data
    @Builder
    public static class DailyMatchResponse {
        private Long matchId;
        private Long profileId;
        private String firstName;
        private String lastName;
        private Integer age;
        private String gotra;
        private String currentCity;
        private String primaryPhotoUrl;
        private String qualification;
        private String profession;
        private Integer compatibilityScore;
        private LocalDate matchDate;

        // Score breakdown
        private Integer familyScore;
        private Integer educationScore;
        private Integer locationScore;
        private Integer lifestyleScore;
        private Integer preferenceScore;
    }

    // =============================================
    //  Interests
    // =============================================
    @Data
    public static class SendInterestRequest {
        private String message;
    }

    @Data
    public static class DeclineInterestRequest {
        private String reason;
    }

    @Data
    @Builder
    public static class InterestResponse {
        private Long interestId;
        private Long profileId;
        private String firstName;
        private String lastName;
        private Integer age;
        private String gotra;
        private String currentCity;
        private String primaryPhotoUrl;
        private String status;
        private String message;
        private String declineReason;
        private String sentAt;
        private String respondedAt;
        private String direction; // SENT or RECEIVED
    }

    @Data
    @Builder
    public static class InterestCountResponse {
        private long pendingReceived;
        private long totalSent;
        private long totalReceived;
        private long accepted;
    }
}
