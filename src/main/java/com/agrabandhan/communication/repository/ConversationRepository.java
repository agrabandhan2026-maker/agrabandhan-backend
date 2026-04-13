package com.agrabandhan.communication.repository;

import com.agrabandhan.communication.entity.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    @Query("SELECT c FROM Conversation c " +
           "LEFT JOIN FETCH c.profileA pa " +
           "LEFT JOIN FETCH c.profileB pb " +
           "LEFT JOIN FETCH pa.photos " +
           "LEFT JOIN FETCH pb.photos " +
           "WHERE (c.profileA.id = :profileId OR c.profileB.id = :profileId) " +
           "AND c.active = true " +
           "ORDER BY c.lastMessageAt DESC NULLS LAST")
    Page<Conversation> findMyConversations(Long profileId, Pageable pageable);

    @Query("SELECT c FROM Conversation c WHERE " +
           "(c.profileA.id = :profileId1 AND c.profileB.id = :profileId2) OR " +
           "(c.profileA.id = :profileId2 AND c.profileB.id = :profileId1)")
    Optional<Conversation> findByProfiles(Long profileId1, Long profileId2);

    @Query("SELECT COUNT(DISTINCT m.conversation.id) FROM ChatMessage m " +
           "JOIN m.conversation c " +
           "WHERE (c.profileA.id = :profileId OR c.profileB.id = :profileId) " +
           "AND m.senderProfile.id != :profileId " +
           "AND m.read = false")
    long countUnreadConversations(Long profileId);
}
