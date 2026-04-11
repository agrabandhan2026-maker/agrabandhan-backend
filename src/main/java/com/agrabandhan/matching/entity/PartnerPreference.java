package com.agrabandhan.matching.entity;

import com.agrabandhan.common.entity.BaseEntity;
import com.agrabandhan.profile.entity.Profile;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "partner_preferences")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PartnerPreference extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;

    @Column(name = "age_min")
    @Builder.Default
    private Integer ageMin = 18;

    @Column(name = "age_max")
    @Builder.Default
    private Integer ageMax = 40;

    @Column(name = "height_min")
    private Integer heightMin;

    @Column(name = "height_max")
    private Integer heightMax;

    // Comma-separated values: NEVER_MARRIED,DIVORCED
    @Column(name = "preferred_marital_statuses", length = 255)
    private String preferredMaritalStatuses;

    @Column(name = "min_qualification", length = 50)
    private String minQualification;

    // Comma-separated: PRIVATE,BUSINESS,GOVERNMENT
    @Column(name = "preferred_employed_in", length = 255)
    private String preferredEmployedIn;

    @Column(name = "min_income_range", length = 50)
    private String minIncomeRange;

    // Comma-separated cities
    @Column(name = "preferred_cities", length = 500)
    private String preferredCities;

    // Comma-separated states
    @Column(name = "preferred_states", length = 255)
    private String preferredStates;

    @Column(name = "preferred_country", length = 50)
    @Builder.Default
    private String preferredCountry = "India";

    @Column(name = "preferred_diet", length = 20)
    private String preferredDiet;

    @Column(name = "smoking_acceptable")
    @Builder.Default
    private Boolean smokingAcceptable = false;

    @Column(name = "drinking_acceptable")
    @Builder.Default
    private Boolean drinkingAcceptable = false;

    @Column(name = "preferred_family_type", length = 20)
    private String preferredFamilyType;

    // Comma-separated gotra names to exclude beyond auto same-gotra exclusion
    @Column(name = "exclude_gotras", length = 255)
    private String excludeGotras;

    @Enumerated(EnumType.STRING)
    @Column(name = "manglik_preference", length = 20)
    private ManglikPreference manglikPreference;

    public enum ManglikPreference {
        YES, NO, DOESNT_MATTER
    }
}
