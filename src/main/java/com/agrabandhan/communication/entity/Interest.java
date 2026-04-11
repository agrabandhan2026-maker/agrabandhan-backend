package com.agrabandhan.communication.entity;

import com.agrabandhan.common.entity.BaseEntity;
import com.agrabandhan.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interests", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"sender_profile_id", "receiver_profile_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_profile_id", nullable = false)
    private Profile senderProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_profile_id", nullable = false)
    private Profile receiverProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private InterestStatus status = InterestStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "decline_reason", length = 255)
    private String declineReason;

    @Column(name = "sent_at", nullable = false)
    @Builder.Default
    private LocalDateTime sentAt = LocalDateTime.now();

    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    public enum InterestStatus {
        PENDING, ACCEPTED, DECLINED, WITHDRAWN
    }
}
