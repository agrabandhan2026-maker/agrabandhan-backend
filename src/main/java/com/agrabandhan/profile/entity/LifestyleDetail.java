package com.agrabandhan.profile.entity;

import com.agrabandhan.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "lifestyle_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LifestyleDetail extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Diet diet;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Habit smoking;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Habit drinking;

    @Column(columnDefinition = "TEXT")
    private String hobbies;

    @Column(columnDefinition = "TEXT")
    private String interests;

    @Column(name = "languages_spoken", length = 255)
    private String languagesSpoken;

    public enum Diet {
        VEGETARIAN, JAIN, EGGETARIAN, NON_VEGETARIAN
    }

    public enum Habit {
        NO, OCCASIONALLY, YES
    }
}
