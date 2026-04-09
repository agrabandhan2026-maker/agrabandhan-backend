package com.agrabandhan.profile.entity;

import com.agrabandhan.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "education_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationDetail extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "highest_qualification", nullable = false, length = 50)
    private Qualification highestQualification;

    @Column(name = "qualification_detail", length = 100)
    private String qualificationDetail;

    @Column(length = 200)
    private String institution;

    @Column(length = 200)
    private String university;

    @Column(name = "passing_year")
    private Integer passingYear;

    @Column(name = "additional_qualification", length = 200)
    private String additionalQualification;

    public enum Qualification {
        BELOW_10TH, TENTH, TWELFTH, DIPLOMA, GRADUATE,
        POST_GRADUATE, DOCTORATE, PROFESSIONAL
    }
}
