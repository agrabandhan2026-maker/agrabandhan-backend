package com.agrabandhan.community.entity;

import com.agrabandhan.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "community_endorsements", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"profile_id", "endorser_profile_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CommunityEndorsement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "endorser_profile_id", nullable = false)
    private Profile endorserProfile;

    @Column(name = "endorsement_text", nullable = false, columnDefinition = "TEXT")
    private String endorsementText;

    @Column(length = 50)
    private String relationship;

    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private boolean approved = false;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
