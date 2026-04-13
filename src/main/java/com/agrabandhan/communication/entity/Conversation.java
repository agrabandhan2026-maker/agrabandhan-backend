package com.agrabandhan.communication.entity;

import com.agrabandhan.common.entity.BaseEntity;
import com.agrabandhan.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "conversations", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"profile_a_id", "profile_b_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_a_id", nullable = false)
    private Profile profileA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_b_id", nullable = false)
    private Profile profileB;

    @Column(name = "last_message_at")
    private LocalDateTime lastMessageAt;

    @Column(name = "last_message_preview", length = 255)
    private String lastMessagePreview;

    @Column(name = "profile_a_shared_contact", nullable = false)
    @Builder.Default
    private boolean profileASharedContact = false;

    @Column(name = "profile_b_shared_contact", nullable = false)
    @Builder.Default
    private boolean profileBSharedContact = false;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * Check if a given profile is part of this conversation.
     */
    public boolean involvesProfile(Long profileId) {
        return profileA.getId().equals(profileId) || profileB.getId().equals(profileId);
    }

    /**
     * Get the other profile in the conversation.
     */
    public Profile getOtherProfile(Long myProfileId) {
        return profileA.getId().equals(myProfileId) ? profileB : profileA;
    }

    /**
     * Check if the given profile has shared their contact.
     */
    public boolean hasSharedContact(Long profileId) {
        if (profileA.getId().equals(profileId)) return profileASharedContact;
        if (profileB.getId().equals(profileId)) return profileBSharedContact;
        return false;
    }

    /**
     * Mark contact as shared for the given profile.
     */
    public void shareContact(Long profileId) {
        if (profileA.getId().equals(profileId)) this.profileASharedContact = true;
        if (profileB.getId().equals(profileId)) this.profileBSharedContact = true;
    }
}
