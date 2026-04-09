package com.agrabandhan.profile.entity;

import com.agrabandhan.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "family_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FamilyDetail extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;

    @Column(name = "father_name", length = 100)
    private String fatherName;

    @Column(name = "father_occupation", length = 100)
    private String fatherOccupation;

    @Column(name = "mother_name", length = 100)
    private String motherName;

    @Column(name = "mother_occupation", length = 100)
    private String motherOccupation;

    @Enumerated(EnumType.STRING)
    @Column(name = "family_type", length = 20)
    private FamilyType familyType;

    @Enumerated(EnumType.STRING)
    @Column(name = "family_values", length = 20)
    private FamilyValues familyValues;

    @Enumerated(EnumType.STRING)
    @Column(name = "family_affluence", length = 20)
    private FamilyAffluence familyAffluence;

    @Column(name = "brothers_count")
    @Builder.Default
    private Integer brothersCount = 0;

    @Column(name = "brothers_married")
    @Builder.Default
    private Integer brothersMarried = 0;

    @Column(name = "sisters_count")
    @Builder.Default
    private Integer sistersCount = 0;

    @Column(name = "sisters_married")
    @Builder.Default
    private Integer sistersMarried = 0;

    // Vyapar (Business) - Baniya community specific
    @Column(name = "family_business_type", length = 100)
    private String familyBusinessType;

    @Column(name = "business_nature", length = 100)
    private String businessNature;

    @Column(name = "business_turnover_range", length = 50)
    private String businessTurnoverRange;

    @Column(name = "business_locations", length = 255)
    private String businessLocations;

    @Column(name = "about_family", columnDefinition = "TEXT")
    private String aboutFamily;

    public enum FamilyType {
        JOINT, NUCLEAR, OTHER
    }

    public enum FamilyValues {
        TRADITIONAL, MODERATE, LIBERAL
    }

    public enum FamilyAffluence {
        AFFLUENT, UPPER_MIDDLE, MIDDLE, LOWER_MIDDLE
    }
}
