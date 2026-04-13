package com.agrabandhan.communication.controller;

import com.agrabandhan.auth.entity.User;
import com.agrabandhan.common.dto.ApiResponse;
import com.agrabandhan.common.dto.PagedResponse;
import com.agrabandhan.communication.dto.ChatDto.*;
import com.agrabandhan.communication.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
@Tag(name = "Chat", description = "In-app messaging, conversations, contact sharing, block & report")
public class ChatController {

    private final ChatService chatService;

    // =============================================
    //  Conversations
    // =============================================
    @GetMapping("/conversations")
    @Operation(summary = "Get my conversations (sorted by last message)")
    public ResponseEntity<ApiResponse<PagedResponse<ConversationResponse>>> getConversations(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<ConversationResponse> conversations = chatService.getMyConversations(user.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(conversations));
    }

    // =============================================
    //  Messages
    // =============================================
    @PostMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "Send a message in a conversation")
    public ResponseEntity<ApiResponse<MessageResponse>> sendMessage(
            @AuthenticationPrincipal User user,
            @PathVariable Long conversationId,
            @Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = chatService.sendMessage(user.getId(), conversationId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Message sent", response));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    @Operation(summary = "Get messages in a conversation (paginated, newest first)")
    public ResponseEntity<ApiResponse<PagedResponse<MessageResponse>>> getMessages(
            @AuthenticationPrincipal User user,
            @PathVariable Long conversationId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
        PagedResponse<MessageResponse> messages = chatService.getMessages(user.getId(), conversationId, page, size);
        return ResponseEntity.ok(ApiResponse.success(messages));
    }

    @GetMapping("/conversations/{conversationId}/poll")
    @Operation(summary = "Poll for new messages since a timestamp (call every 3 seconds)")
    public ResponseEntity<ApiResponse<PollResponse>> pollMessages(
            @AuthenticationPrincipal User user,
            @PathVariable Long conversationId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        PollResponse response = chatService.pollMessages(user.getId(), conversationId, since);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/conversations/{conversationId}/read")
    @Operation(summary = "Mark all messages in a conversation as read")
    public ResponseEntity<ApiResponse<Void>> markAsRead(
            @AuthenticationPrincipal User user,
            @PathVariable Long conversationId) {
        chatService.markAsRead(user.getId(), conversationId);
        return ResponseEntity.ok(ApiResponse.success("Messages marked as read"));
    }

    // =============================================
    //  Unread Count (for badges)
    // =============================================
    @GetMapping("/unread")
    @Operation(summary = "Get total unread message count (for app badge)")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            @AuthenticationPrincipal User user) {
        UnreadCountResponse count = chatService.getUnreadCount(user.getId());
        return ResponseEntity.ok(ApiResponse.success(count));
    }

    // =============================================
    //  Contact Sharing
    // =============================================
    @PostMapping("/conversations/{conversationId}/share-contact")
    @Operation(summary = "Share my phone number with the other person in this conversation")
    public ResponseEntity<ApiResponse<ContactInfoResponse>> shareContact(
            @AuthenticationPrincipal User user,
            @PathVariable Long conversationId) {
        ContactInfoResponse response = chatService.shareMyContact(user.getId(), conversationId);
        return ResponseEntity.ok(ApiResponse.success("Contact shared", response));
    }

    @GetMapping("/conversations/{conversationId}/contact")
    @Operation(summary = "Get the other person's shared contact info (if they shared)")
    public ResponseEntity<ApiResponse<ContactInfoResponse>> getSharedContact(
            @AuthenticationPrincipal User user,
            @PathVariable Long conversationId) {
        ContactInfoResponse response = chatService.getSharedContact(user.getId(), conversationId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // =============================================
    //  Block & Report
    // =============================================
    @PostMapping("/block/{profileId}")
    @Operation(summary = "Block a profile (stops all interaction, deactivates conversation)")
    public ResponseEntity<ApiResponse<Void>> blockProfile(
            @AuthenticationPrincipal User user,
            @PathVariable Long profileId,
            @RequestBody(required = false) BlockRequest request) {
        chatService.blockProfile(user.getId(), profileId, request);
        return ResponseEntity.ok(ApiResponse.success("Profile blocked"));
    }

    @DeleteMapping("/block/{profileId}")
    @Operation(summary = "Unblock a profile")
    public ResponseEntity<ApiResponse<Void>> unblockProfile(
            @AuthenticationPrincipal User user,
            @PathVariable Long profileId) {
        chatService.unblockProfile(user.getId(), profileId);
        return ResponseEntity.ok(ApiResponse.success("Profile unblocked"));
    }

    @PostMapping("/report/{profileId}")
    @Operation(summary = "Report a profile for moderation review")
    public ResponseEntity<ApiResponse<Void>> reportProfile(
            @AuthenticationPrincipal User user,
            @PathVariable Long profileId,
            @Valid @RequestBody ReportRequest request) {
        chatService.reportProfile(user.getId(), profileId, request);
        return ResponseEntity.ok(ApiResponse.success("Report submitted. Our team will review it shortly."));
    }
}
