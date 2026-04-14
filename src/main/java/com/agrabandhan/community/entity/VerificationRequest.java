package com.agrabandhan.community.entity;

import com.agrabandhan.auth.entity.User;
import com.agrabandhan.common.entity.BaseEntity;
import com.agrabandhan.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_requests")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class VerificationRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false, length = 20)
    private DocumentType documentType;

    @Column(name = "document_key", nullable = false, length = 500)
    private String documentKey;

    @Column(name = "selfie_key", length = 500)
    private String selfieKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private VerificationStatus status = VerificationStatus.PENDING;

    @Column(name = "rejection_reason", length = 255)
    private String rejectionReason;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private LocalDateTime reviewedAt;

    public enum DocumentType {
        AADHAAR, PAN, VOTER_ID, PASSPORT, DRIVING_LICENSE
    }

    public enum VerificationStatus {
        PENDING, APPROVED, REJECTED
    }
}
