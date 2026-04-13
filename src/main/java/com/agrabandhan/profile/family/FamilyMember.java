package com.agrabandhan.profile.family;

import com.agrabandhan.auth.entity.User;
import com.agrabandhan.common.entity.BaseEntity;
import com.agrabandhan.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "family_members")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyMember extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Relationship relationship;

    @Column(name = "phone_number", length = 15)
    private String phoneNumber;

    // Permissions
    @Column(name = "can_search") @Builder.Default private boolean canSearch = true;
    @Column(name = "can_shortlist") @Builder.Default private boolean canShortlist = true;
    @Column(name = "can_send_interest") @Builder.Default private boolean canSendInterest = false;
    @Column(name = "can_accept_interest") @Builder.Default private boolean canAcceptInterest = false;
    @Column(name = "can_chat") @Builder.Default private boolean canChat = false;
    @Column(name = "can_edit_profile") @Builder.Default private boolean canEditProfile = false;

    @Column(name = "invite_token", unique = true, length = 100)
    private String inviteToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "invite_status", nullable = false, length = 20)
    @Builder.Default
    private InviteStatus inviteStatus = InviteStatus.PENDING;

    @Column(name = "invite_expires_at")
    private LocalDateTime inviteExpiresAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    public enum Relationship {
        FATHER, MOTHER, BROTHER, SISTER, SELF
    }

    public enum InviteStatus {
        PENDING, ACCEPTED, EXPIRED
    }
}
