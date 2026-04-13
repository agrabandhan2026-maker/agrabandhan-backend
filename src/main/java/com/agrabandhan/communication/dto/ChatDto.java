package com.agrabandhan.communication.dto;

import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class ChatDto {

    // =============================================
    //  Send Message
    // =============================================
    @Data
    public static class SendMessageRequest {
        @NotBlank(message = "Message cannot be empty")
        @Size(max = 2000, message = "Message cannot exceed 2000 characters")
        private String content;
    }

    // =============================================
    //  Message Response
    // =============================================
    @Data
    @Builder
    public static class MessageResponse {
        private Long messageId;
        private Long conversationId;
        private Long senderProfileId;
        private String senderName;
        private String content;
        private String messageType;
        private boolean read;
        private LocalDateTime readAt;
        private LocalDateTime createdAt;
        private boolean isMine;
    }

    // =============================================
    //  Conversation List Response
    // =============================================
    @Data
    @Builder
    public static class ConversationResponse {
        private Long conversationId;
        private Long otherProfileId;
        private String otherProfileName;
        private String otherProfileGotra;
        private String otherProfileCity;
        private String otherProfilePhotoUrl;
        private String lastMessage;
        private LocalDateTime lastMessageAt;
        private long unreadCount;
        private boolean myContactShared;
        private boolean theirContactShared;
    }

    // =============================================
    //  Contact Share
    // =============================================
    @Data
    @Builder
    public static class ContactInfoResponse {
        private String phoneNumber;
        private String email;
        private String name;
        private boolean shared;
    }

    // =============================================
    //  Block
    // =============================================
    @Data
    public static class BlockRequest {
        private String reason;
    }

    // =============================================
    //  Report
    // =============================================
    @Data
    public static class ReportRequest {
        @NotNull(message = "Report reason is required")
        private String reason; // FAKE_PROFILE, INAPPROPRIATE_PHOTO, HARASSMENT, SPAM, WRONG_COMMUNITY, OTHER
        private String description;
    }

    // =============================================
    //  Unread Count
    // =============================================
    @Data
    @Builder
    public static class UnreadCountResponse {
        private long totalUnreadMessages;
        private long unreadConversations;
    }

    // =============================================
    //  Poll Response (for new messages)
    // =============================================
    @Data
    @Builder
    public static class PollResponse {
        private java.util.List<MessageResponse> newMessages;
        private long totalUnread;
        private LocalDateTime serverTime;
    }
}
