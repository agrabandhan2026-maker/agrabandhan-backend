package com.agrabandhan.communication.repository;

import com.agrabandhan.communication.entity.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByConversationIdOrderByCreatedAtDesc(Long conversationId, Pageable pageable);

    // For polling: get messages after a certain timestamp
    @Query("SELECT m FROM ChatMessage m WHERE m.conversation.id = :conversationId " +
           "AND m.createdAt > :since ORDER BY m.createdAt ASC")
    List<ChatMessage> findNewMessages(Long conversationId, LocalDateTime since);

    // Count unread messages in a conversation for a specific user
    @Query("SELECT COUNT(m) FROM ChatMessage m WHERE m.conversation.id = :conversationId " +
           "AND m.senderProfile.id != :profileId AND m.read = false")
    long countUnreadInConversation(Long conversationId, Long profileId);

    // Mark all messages in a conversation as read (messages NOT sent by me)
    @Modifying
    @Query("UPDATE ChatMessage m SET m.read = true, m.readAt = :now " +
           "WHERE m.conversation.id = :conversationId " +
           "AND m.senderProfile.id != :profileId AND m.read = false")
    int markAsRead(Long conversationId, Long profileId, LocalDateTime now);

    // Total unread count across all conversations
    @Query("SELECT COUNT(m) FROM ChatMessage m " +
           "JOIN m.conversation c " +
           "WHERE (c.profileA.id = :profileId OR c.profileB.id = :profileId) " +
           "AND m.senderProfile.id != :profileId AND m.read = false")
    long countTotalUnread(Long profileId);
}
