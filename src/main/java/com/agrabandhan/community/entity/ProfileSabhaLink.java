package com.agrabandhan.community.entity;

import com.agrabandhan.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "profile_sabha_links", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"profile_id", "sabha_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProfileSabhaLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sabha_id", nullable = false)
    private SamajSabha sabha;

    @Column(name = "verified_by_leader", nullable = false)
    @Builder.Default
    private boolean verifiedByLeader = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
