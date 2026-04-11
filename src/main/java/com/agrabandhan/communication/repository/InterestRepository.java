package com.agrabandhan.communication.repository;

import com.agrabandhan.communication.entity.Interest;
import com.agrabandhan.communication.entity.Interest.InterestStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterestRepository extends JpaRepository<Interest, Long> {

    Optional<Interest> findBySenderProfileIdAndReceiverProfileId(Long senderId, Long receiverId);

    boolean existsBySenderProfileIdAndReceiverProfileId(Long senderId, Long receiverId);

    // Interests I sent
    @Query("SELECT i FROM Interest i " +
           "JOIN FETCH i.receiverProfile rp " +
           "LEFT JOIN FETCH rp.photos " +
           "WHERE i.senderProfile.id = :profileId AND i.status = :status " +
           "ORDER BY i.sentAt DESC")
    Page<Interest> findSentInterests(Long profileId, InterestStatus status, Pageable pageable);

    // Interests I received
    @Query("SELECT i FROM Interest i " +
           "JOIN FETCH i.senderProfile sp " +
           "LEFT JOIN FETCH sp.photos " +
           "WHERE i.receiverProfile.id = :profileId AND i.status = :status " +
           "ORDER BY i.sentAt DESC")
    Page<Interest> findReceivedInterests(Long profileId, InterestStatus status, Pageable pageable);

    // All sent (any status)
    @Query("SELECT i FROM Interest i " +
           "JOIN FETCH i.receiverProfile rp " +
           "LEFT JOIN FETCH rp.photos " +
           "WHERE i.senderProfile.id = :profileId " +
           "ORDER BY i.sentAt DESC")
    Page<Interest> findAllSent(Long profileId, Pageable pageable);

    // All received (any status)
    @Query("SELECT i FROM Interest i " +
           "JOIN FETCH i.senderProfile sp " +
           "LEFT JOIN FETCH sp.photos " +
           "WHERE i.receiverProfile.id = :profileId " +
           "ORDER BY i.sentAt DESC")
    Page<Interest> findAllReceived(Long profileId, Pageable pageable);

    // Mutually accepted (both sides accepted - for chat eligibility)
    @Query("SELECT CASE WHEN COUNT(i) > 0 THEN true ELSE false END FROM Interest i " +
           "WHERE ((i.senderProfile.id = :profileId1 AND i.receiverProfile.id = :profileId2) " +
           "OR (i.senderProfile.id = :profileId2 AND i.receiverProfile.id = :profileId1)) " +
           "AND i.status = 'ACCEPTED'")
    boolean areMutuallyConnected(Long profileId1, Long profileId2);

    // Count pending received
    long countByReceiverProfileIdAndStatus(Long profileId, InterestStatus status);
}
