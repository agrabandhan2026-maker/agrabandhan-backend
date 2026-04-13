package com.agrabandhan.profile.family;

import com.agrabandhan.auth.entity.User;
import com.agrabandhan.common.entity.BaseEntity;
import com.agrabandhan.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "voice_introductions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VoiceIntroduction extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recorded_by_user_id", nullable = false)
    private User recordedByUser;

    @Column(name = "recorded_by_name", nullable = false, length = 100)
    private String recordedByName;

    @Column(name = "recorded_by_relationship", length = 20)
    private String recordedByRelationship;

    @Column(name = "audio_key", nullable = false, length = 500)
    private String audioKey;

    @Column(name = "duration_seconds", nullable = false)
    private Integer durationSeconds;

    @Column(name = "file_size_bytes")
    private Long fileSizeBytes;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;
}
