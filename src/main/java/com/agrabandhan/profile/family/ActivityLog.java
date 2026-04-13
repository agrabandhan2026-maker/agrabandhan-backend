package com.agrabandhan.profile.family;

import com.agrabandhan.auth.entity.User;
import com.agrabandhan.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "profile_activity_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_user_id", nullable = false)
    private User actorUser;

    @Column(name = "actor_name", nullable = false, length = 100)
    private String actorName;

    @Column(name = "actor_relationship", length = 20)
    private String actorRelationship;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 50)
    private ActionType actionType;

    @Column(name = "action_detail", columnDefinition = "TEXT")
    private String actionDetail;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ActionType {
        PROFILE_UPDATED, PHOTO_UPLOADED, PHOTO_DELETED,
        INTEREST_SENT, INTEREST_ACCEPTED, INTEREST_DECLINED,
        SEARCH_PERFORMED, PROFILE_SHORTLISTED,
        FAMILY_MEMBER_ADDED, FAMILY_MEMBER_REMOVED,
        CONTACT_SHARED, MESSAGE_SENT,
        PREFERENCE_UPDATED, VOICE_INTRO_UPLOADED
    }
}
