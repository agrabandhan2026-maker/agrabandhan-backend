package com.agrabandhan.profile.family;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class FamilyDto {

    // =============================================
    //  Add Family Member
    // =============================================
    @Data
    public static class AddFamilyMemberRequest {
        @NotBlank(message = "Name is required")
        private String name;

        @NotNull(message = "Relationship is required")
        private FamilyMember.Relationship relationship;

        private String phoneNumber;

        // Permissions
        private Boolean canSearch;
        private Boolean canShortlist;
        private Boolean canSendInterest;
        private Boolean canAcceptInterest;
        private Boolean canChat;
        private Boolean canEditProfile;
    }

    @Data
    public static class UpdatePermissionsRequest {
        private Boolean canSearch;
        private Boolean canShortlist;
        private Boolean canSendInterest;
        private Boolean canAcceptInterest;
        private Boolean canChat;
        private Boolean canEditProfile;
    }

    // =============================================
    //  Responses
    // =============================================
    @Data
    @Builder
    public static class FamilyMemberResponse {
        private Long id;
        private String name;
        private String relationship;
        private String phoneNumber;
        private boolean canSearch;
        private boolean canShortlist;
        private boolean canSendInterest;
        private boolean canAcceptInterest;
        private boolean canChat;
        private boolean canEditProfile;
        private String inviteStatus;
        private String inviteToken;
        private boolean active;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class ActivityLogResponse {
        private Long id;
        private String actorName;
        private String actorRelationship;
        private String actionType;
        private String actionDetail;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class VoiceIntroResponse {
        private Long id;
        private String audioUrl;
        private String recordedByName;
        private String recordedByRelationship;
        private Integer durationSeconds;
        private boolean active;
        private LocalDateTime createdAt;
    }

    @Data
    @Builder
    public static class FamilyDashboardResponse {
        private Long profileId;
        private String profileName;
        private List<FamilyMemberResponse> members;
        private VoiceIntroResponse voiceIntro;
        private int memberCount;
        private int maxMembers;
    }
}
