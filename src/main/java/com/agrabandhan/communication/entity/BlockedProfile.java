package com.agrabandhan.communication.entity;

import com.agrabandhan.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "blocked_profiles", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"blocker_profile_id", "blocked_profile_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BlockedProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_profile_id", nullable = false)
    private Profile blockerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_profile_id", nullable = false)
    private Profile blockedProfile;

    @Column(length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
