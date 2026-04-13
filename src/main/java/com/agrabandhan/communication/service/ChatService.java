package com.agrabandhan.communication.service;

import com.agrabandhan.auth.entity.User;
import com.agrabandhan.auth.repository.UserRepository;
import com.agrabandhan.common.dto.PagedResponse;
import com.agrabandhan.common.exception.BadRequestException;
import com.agrabandhan.common.exception.ResourceNotFoundException;
import com.agrabandhan.communication.dto.ChatDto.*;
import com.agrabandhan.communication.entity.*;
import com.agrabandhan.communication.entity.ChatMessage.MessageType;
import com.agrabandhan.communication.repository.*;
import com.agrabandhan.profile.entity.Profile;
import com.agrabandhan.profile.entity.ProfilePhoto;
import com.agrabandhan.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository messageRepository;
    private final InterestRepository interestRepository;
    private final BlockedProfileRepository blockedRepository;
    private final ProfileReportRepository reportRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Value("${agrabandhan.r2.public-url:}")
    private String photoBaseUrl;

    // =============================================
    //  Send Message
    // =============================================
    @Transactional
    public MessageResponse sendMessage(Long userId, Long conversationId, SendMessageRequest request) {
        Profile myProfile = getMyProfile(userId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        validateConversationAccess(conversation, myProfile.getId());

        // Check if blocked
        Profile otherProfile = conversation.getOtherProfile(myProfile.getId());
        if (isBlocked(myProfile.getId(), otherProfile.getId())) {
            throw new BadRequestException("You cannot send messages to this profile");
        }

        ChatMessage message = ChatMessage.builder()
                .conversation(conversation)
                .senderProfile(myProfile)
                .content(request.getContent())
                .messageType(MessageType.TEXT)
                .build();

        message = messageRepository.save(message);

        // Update conversation preview
        String preview = request.getContent().length() > 100
                ? request.getContent().substring(0, 100) + "..."
                : request.getContent();
        conversation.setLastMessageAt(message.getCreatedAt());
        conversation.setLastMessagePreview(preview);
        conversationRepository.save(conversation);

        log.info("Message sent: conversationId={}, senderId={}", conversationId, myProfile.getId());

        return toMessageResponse(message, myProfile.getId());
    }

    // =============================================
    //  Get Conversations
    // =============================================
    @Transactional(readOnly = true)
    public PagedResponse<ConversationResponse> getMyConversations(Long userId, int page, int size) {
        Profile myProfile = getMyProfile(userId);

        Page<Conversation> conversations = conversationRepository.findMyConversations(
                myProfile.getId(), PageRequest.of(page, Math.min(size, 50)));

        Page<ConversationResponse> responsePage = conversations.map(c -> {
            Profile other = c.getOtherProfile(myProfile.getId());
            long unread = messageRepository.countUnreadInConversation(c.getId(), myProfile.getId());

            return ConversationResponse.builder()
                    .conversationId(c.getId())
                    .otherProfileId(other.getId())
                    .otherProfileName(other.getFirstName() + " " + other.getLastName())
                    .otherProfileGotra(other.getGotra().getDisplayName())
                    .otherProfileCity(other.getCurrentCity())
                    .otherProfilePhotoUrl(getPrimaryPhoto(other))
                    .lastMessage(c.getLastMessagePreview())
                    .lastMessageAt(c.getLastMessageAt())
                    .unreadCount(unread)
                    .myContactShared(c.hasSharedContact(myProfile.getId()))
                    .theirContactShared(c.hasSharedContact(other.getId()))
                    .build();
        });

        return PagedResponse.from(responsePage);
    }

    // =============================================
    //  Get Messages (with pagination)
    // =============================================
    @Transactional(readOnly = true)
    public PagedResponse<MessageResponse> getMessages(Long userId, Long conversationId, int page, int size) {
        Profile myProfile = getMyProfile(userId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        validateConversationAccess(conversation, myProfile.getId());

        Page<ChatMessage> messages = messageRepository.findByConversationIdOrderByCreatedAtDesc(
                conversationId, PageRequest.of(page, Math.min(size, 50)));

        Page<MessageResponse> responsePage = messages.map(m -> toMessageResponse(m, myProfile.getId()));
        return PagedResponse.from(responsePage);
    }

    // =============================================
    //  Poll for New Messages (every 3 seconds)
    // =============================================
    @Transactional(readOnly = true)
    public PollResponse pollMessages(Long userId, Long conversationId, LocalDateTime since) {
        Profile myProfile = getMyProfile(userId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        validateConversationAccess(conversation, myProfile.getId());

        LocalDateTime pollSince = since != null ? since : LocalDateTime.now().minusSeconds(5);

        List<ChatMessage> newMessages = messageRepository.findNewMessages(conversationId, pollSince);
        long totalUnread = messageRepository.countTotalUnread(myProfile.getId());

        return PollResponse.builder()
                .newMessages(newMessages.stream()
                        .map(m -> toMessageResponse(m, myProfile.getId())).toList())
                .totalUnread(totalUnread)
                .serverTime(LocalDateTime.now())
                .build();
    }

    // =============================================
    //  Mark Messages as Read
    // =============================================
    @Transactional
    public void markAsRead(Long userId, Long conversationId) {
        Profile myProfile = getMyProfile(userId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        validateConversationAccess(conversation, myProfile.getId());

        int updated = messageRepository.markAsRead(conversationId, myProfile.getId(), LocalDateTime.now());
        log.debug("Marked {} messages as read in conversation {}", updated, conversationId);
    }

    // =============================================
    //  Unread Count (for badge)
    // =============================================
    @Transactional(readOnly = true)
    public UnreadCountResponse getUnreadCount(Long userId) {
        Profile myProfile = getMyProfile(userId);
        long totalUnread = messageRepository.countTotalUnread(myProfile.getId());
        long unreadConversations = conversationRepository.countUnreadConversations(myProfile.getId());

        return UnreadCountResponse.builder()
                .totalUnreadMessages(totalUnread)
                .unreadConversations(unreadConversations)
                .build();
    }

    // =============================================
    //  Contact Sharing
    // =============================================
    @Transactional
    public ContactInfoResponse shareMyContact(Long userId, Long conversationId) {
        Profile myProfile = getMyProfile(userId);
        User myUser = myProfile.getUser();
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        validateConversationAccess(conversation, myProfile.getId());

        conversation.shareContact(myProfile.getId());
        conversationRepository.save(conversation);

        // Send system message
        ChatMessage systemMsg = ChatMessage.builder()
                .conversation(conversation)
                .senderProfile(myProfile)
                .content(myProfile.getFirstName() + " shared their contact information")
                .messageType(MessageType.CONTACT_SHARED)
                .build();
        messageRepository.save(systemMsg);

        conversation.setLastMessageAt(systemMsg.getCreatedAt());
        conversation.setLastMessagePreview("Contact information shared");
        conversationRepository.save(conversation);

        return ContactInfoResponse.builder()
                .phoneNumber(myUser.getPhoneNumber())
                .name(myProfile.getFirstName() + " " + myProfile.getLastName())
                .shared(true)
                .build();
    }

    @Transactional(readOnly = true)
    public ContactInfoResponse getSharedContact(Long userId, Long conversationId) {
        Profile myProfile = getMyProfile(userId);
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", "id", conversationId));

        validateConversationAccess(conversation, myProfile.getId());

        Profile otherProfile = conversation.getOtherProfile(myProfile.getId());

        if (!conversation.hasSharedContact(otherProfile.getId())) {
            return ContactInfoResponse.builder().shared(false).build();
        }

        User otherUser = otherProfile.getUser();
        return ContactInfoResponse.builder()
                .phoneNumber(otherUser.getPhoneNumber())
                .name(otherProfile.getFirstName() + " " + otherProfile.getLastName())
                .shared(true)
                .build();
    }

    // =============================================
    //  Create Conversation (called when interest accepted)
    // =============================================
    @Transactional
    public Conversation getOrCreateConversation(Long profileId1, Long profileId2) {
        // Ensure consistent ordering (smaller ID = profileA)
        Long profileAId = Math.min(profileId1, profileId2);
        Long profileBId = Math.max(profileId1, profileId2);

        return conversationRepository.findByProfiles(profileAId, profileBId)
                .orElseGet(() -> {
                    Profile profileA = profileRepository.findById(profileAId)
                            .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", profileAId));
                    Profile profileB = profileRepository.findById(profileBId)
                            .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", profileBId));

                    Conversation conv = Conversation.builder()
                            .profileA(profileA)
                            .profileB(profileB)
                            .build();

                    conv = conversationRepository.save(conv);
                    log.info("Conversation created between profiles {} and {}", profileAId, profileBId);
                    return conv;
                });
    }

    // =============================================
    //  Block & Report
    // =============================================
    @Transactional
    public void blockProfile(Long userId, Long targetProfileId, BlockRequest request) {
        Profile myProfile = getMyProfile(userId);
        Profile targetProfile = profileRepository.findById(targetProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", targetProfileId));

        if (myProfile.getId().equals(targetProfile.getId())) {
            throw new BadRequestException("Cannot block yourself");
        }

        if (blockedRepository.existsByBlockerProfileIdAndBlockedProfileId(myProfile.getId(), targetProfile.getId())) {
            throw new BadRequestException("Profile already blocked");
        }

        blockedRepository.save(BlockedProfile.builder()
                .blockerProfile(myProfile)
                .blockedProfile(targetProfile)
                .reason(request != null ? request.getReason() : null)
                .build());

        // Deactivate conversation if exists
        conversationRepository.findByProfiles(myProfile.getId(), targetProfile.getId())
                .ifPresent(conv -> {
                    conv.setActive(false);
                    conversationRepository.save(conv);
                });

        log.info("Profile {} blocked by {}", targetProfileId, myProfile.getId());
    }

    @Transactional
    public void unblockProfile(Long userId, Long targetProfileId) {
        Profile myProfile = getMyProfile(userId);
        blockedRepository.deleteByBlockerProfileIdAndBlockedProfileId(myProfile.getId(), targetProfileId);
        log.info("Profile {} unblocked by {}", targetProfileId, myProfile.getId());
    }

    @Transactional
    public void reportProfile(Long userId, Long targetProfileId, ReportRequest request) {
        Profile myProfile = getMyProfile(userId);
        Profile targetProfile = profileRepository.findById(targetProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", targetProfileId));

        if (myProfile.getId().equals(targetProfile.getId())) {
            throw new BadRequestException("Cannot report yourself");
        }

        if (reportRepository.existsByReporterProfileIdAndReportedProfileId(myProfile.getId(), targetProfile.getId())) {
            throw new BadRequestException("You have already reported this profile");
        }

        ProfileReport.ReportReason reportReason;
        try {
            reportReason = ProfileReport.ReportReason.valueOf(request.getReason().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid report reason. Valid: FAKE_PROFILE, INAPPROPRIATE_PHOTO, HARASSMENT, SPAM, WRONG_COMMUNITY, OTHER");
        }

        reportRepository.save(ProfileReport.builder()
                .reporterProfile(myProfile)
                .reportedProfile(targetProfile)
                .reason(reportReason)
                .description(request.getDescription())
                .build());

        log.info("Profile {} reported by {} for {}", targetProfileId, myProfile.getId(), reportReason);
    }

    // =============================================
    //  Helpers
    // =============================================
    private Profile getMyProfile(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));
    }

    private void validateConversationAccess(Conversation conversation, Long profileId) {
        if (!conversation.involvesProfile(profileId)) {
            throw new BadRequestException("You are not part of this conversation");
        }
        if (!conversation.isActive()) {
            throw new BadRequestException("This conversation is no longer active");
        }
    }

    private boolean isBlocked(Long profileId1, Long profileId2) {
        return blockedRepository.existsByBlockerProfileIdAndBlockedProfileId(profileId1, profileId2)
                || blockedRepository.existsByBlockerProfileIdAndBlockedProfileId(profileId2, profileId1);
    }

    private MessageResponse toMessageResponse(ChatMessage msg, Long myProfileId) {
        return MessageResponse.builder()
                .messageId(msg.getId())
                .conversationId(msg.getConversation().getId())
                .senderProfileId(msg.getSenderProfile().getId())
                .senderName(msg.getSenderProfile().getFirstName())
                .content(msg.getContent())
                .messageType(msg.getMessageType().name())
                .read(msg.isRead())
                .readAt(msg.getReadAt())
                .createdAt(msg.getCreatedAt())
                .isMine(msg.getSenderProfile().getId().equals(myProfileId))
                .build();
    }

    private String getPrimaryPhoto(Profile profile) {
        if (profile.getPhotos() == null) return null;
        return profile.getPhotos().stream()
                .filter(ProfilePhoto::isPrimary).findFirst()
                .map(p -> photoBaseUrl + "/" + p.getThumbnailKey())
                .orElse(null);
    }
}
