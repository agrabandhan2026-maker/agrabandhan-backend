package com.agrabandhan.profile.entity;

import com.agrabandhan.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "profile_photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfilePhoto extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    private Profile profile;

    @Column(name = "original_key", nullable = false, length = 500)
    private String originalKey;

    @Column(name = "medium_key", length = 500)
    private String mediumKey;

    @Column(name = "thumbnail_key", length = 500)
    private String thumbnailKey;

    @Column(name = "display_order", nullable = false)
    @Builder.Default
    private Integer displayOrder = 0;

    @Column(name = "is_primary", nullable = false)
    @Builder.Default
    private boolean primary = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private PhotoVisibility visibility = PhotoVisibility.PUBLIC;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean verified = false;

    public enum PhotoVisibility {
        PUBLIC, CONNECTIONS_ONLY, HIDDEN
    }
}
