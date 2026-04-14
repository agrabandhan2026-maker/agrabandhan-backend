package com.agrabandhan.community.entity;

import com.agrabandhan.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "trust_scores")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TrustScore {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;

    @Column(name = "total_score", nullable = false)
    @Builder.Default
    private Integer totalScore = 0;

    // Breakdown (max total = 100)
    @Column(name = "id_verification_score") @Builder.Default private Integer idVerificationScore = 0;       // Max 20
    @Column(name = "photo_verification_score") @Builder.Default private Integer photoVerificationScore = 0;   // Max 10
    @Column(name = "endorsement_score") @Builder.Default private Integer endorsementScore = 0;               // Max 15 (5 each, max 3)
    @Column(name = "sabha_verification_score") @Builder.Default private Integer sabhaVerificationScore = 0;   // Max 30
    @Column(name = "completeness_score") @Builder.Default private Integer completenessScore = 0;             // Max 10
    @Column(name = "response_rate_score") @Builder.Default private Integer responseRateScore = 0;             // Max 15

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    public void recalculate() {
        this.totalScore = idVerificationScore + photoVerificationScore + endorsementScore
                + sabhaVerificationScore + completenessScore + responseRateScore;
        this.totalScore = Math.min(100, this.totalScore);
        this.updatedAt = LocalDateTime.now();
    }
}
