package com.agrabandhan.matching.entity;

import com.agrabandhan.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "daily_matches", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"profile_id", "matched_profile_id", "match_date"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailyMatch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_profile_id", nullable = false)
    private Profile matchedProfile;

    @Column(name = "compatibility_score", nullable = false)
    @Builder.Default
    private Integer compatibilityScore = 0;

    @Column(name = "match_date", nullable = false)
    @Builder.Default
    private LocalDate matchDate = LocalDate.now();

    // Score breakdown
    @Column(name = "family_score") @Builder.Default private Integer familyScore = 0;
    @Column(name = "education_score") @Builder.Default private Integer educationScore = 0;
    @Column(name = "location_score") @Builder.Default private Integer locationScore = 0;
    @Column(name = "lifestyle_score") @Builder.Default private Integer lifestyleScore = 0;
    @Column(name = "preference_score") @Builder.Default private Integer preferenceScore = 0;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
