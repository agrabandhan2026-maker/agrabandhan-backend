package com.agrabandhan.profile.entity;

import com.agrabandhan.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "profession_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfessionDetail extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false, unique = true)
    private Profile profile;

    @Enumerated(EnumType.STRING)
    @Column(name = "employed_in", length = 30)
    private EmployedIn employedIn;

    @Column(length = 100)
    private String profession;

    @Column(name = "employer_name", length = 200)
    private String employerName;

    @Column(length = 100)
    private String designation;

    @Column(name = "annual_income_range", length = 50)
    private String annualIncomeRange;

    @Column(name = "work_city", length = 100)
    private String workCity;

    @Column(name = "work_country", length = 50)
    @Builder.Default
    private String workCountry = "India";

    public enum EmployedIn {
        PRIVATE, GOVERNMENT, BUSINESS, SELF_EMPLOYED, NOT_WORKING, STUDENT
    }
}
