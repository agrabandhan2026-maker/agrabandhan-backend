package com.agrabandhan.profile.entity;

import com.agrabandhan.auth.entity.User;
import com.agrabandhan.common.entity.BaseEntity;
import com.agrabandhan.common.entity.Gotra;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Profile extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    // --- Personal ---
    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Gender gender;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "height_cm")
    private Integer heightCm;

    @Column(name = "weight_kg")
    private Integer weightKg;

    @Column(length = 20)
    private String complexion;

    @Column(name = "blood_group", length = 5)
    private String bloodGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "marital_status", nullable = false, length = 20)
    @Builder.Default
    private MaritalStatus maritalStatus = MaritalStatus.NEVER_MARRIED;

    @Column(length = 100)
    private String disability;

    // --- Community ---
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gotra gotra;

    @Column(name = "sub_caste", length = 50)
    private String subCaste;

    @Column(name = "mother_gotra", length = 20)
    private String motherGotra;

    // --- Location ---
    @Column(name = "current_city", length = 100)
    private String currentCity;

    @Column(name = "current_state", length = 50)
    private String currentState;

    @Column(name = "current_country", length = 50)
    @Builder.Default
    private String currentCountry = "India";

    @Column(name = "native_city", length = 100)
    private String nativeCity;

    @Column(name = "native_state", length = 50)
    private String nativeState;

    // --- About ---
    @Column(name = "about_me", columnDefinition = "TEXT")
    private String aboutMe;

    @Enumerated(EnumType.STRING)
    @Column(name = "profile_managed_by", length = 20)
    @Builder.Default
    private ProfileManagedBy profileManagedBy = ProfileManagedBy.SELF;

    // --- Completeness ---
    @Column(name = "completeness_score", nullable = false)
    @Builder.Default
    private Integer completenessScore = 0;

    // --- Relationships ---
    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private FamilyDetail familyDetail;

    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private EducationDetail educationDetail;

    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ProfessionDetail professionDetail;

    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private LifestyleDetail lifestyleDetail;

    @OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("displayOrder ASC")
    @Builder.Default
    private List<ProfilePhoto> photos = new ArrayList<>();

    // --- Enums ---
    public enum Gender {
        MALE, FEMALE
    }

    public enum MaritalStatus {
        NEVER_MARRIED, DIVORCED, WIDOWED, AWAITING_DIVORCE
    }

    public enum ProfileManagedBy {
        SELF, PARENT, SIBLING, RELATIVE, FRIEND
    }
}
