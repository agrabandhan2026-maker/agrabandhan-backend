package com.agrabandhan.communication.entity;

import com.agrabandhan.auth.entity.User;
import com.agrabandhan.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "profile_reports", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"reporter_profile_id", "reported_profile_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_profile_id", nullable = false)
    private Profile reporterProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_profile_id", nullable = false)
    private Profile reportedProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ReportReason reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private ReportStatus status = ReportStatus.PENDING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ReportReason {
        FAKE_PROFILE, INAPPROPRIATE_PHOTO, HARASSMENT, SPAM, WRONG_COMMUNITY, OTHER
    }

    public enum ReportStatus {
        PENDING, REVIEWED, ACTION_TAKEN, DISMISSED
    }
}
