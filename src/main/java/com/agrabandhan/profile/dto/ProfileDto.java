package com.agrabandhan.profile.dto;

import com.agrabandhan.common.entity.Gotra;
import com.agrabandhan.profile.entity.*;
import com.agrabandhan.profile.entity.Profile.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public class ProfileDto {

    // =============================================
    //  Step 1: Personal Details (Required)
    // =============================================
    @Data
    public static class PersonalDetailsRequest {
        @NotBlank(message = "First name is required")
        @Size(max = 50)
        private String firstName;

        @NotBlank(message = "Last name is required")
        @Size(max = 50)
        private String lastName;

        @NotNull(message = "Gender is required")
        private Gender gender;

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        private LocalDate dateOfBirth;

        private Integer heightCm;
        private Integer weightKg;
        private String complexion;
        private String bloodGroup;

        @NotNull(message = "Marital status is required")
        private MaritalStatus maritalStatus;

        private String disability;

        @NotNull(message = "Gotra is required")
        private Gotra gotra;

        private String subCaste;
        private String motherGotra;

        private String currentCity;
        private String currentState;
        private String currentCountry;
        private String nativeCity;
        private String nativeState;

        private String aboutMe;
        private ProfileManagedBy profileManagedBy;
    }

    // =============================================
    //  Step 2: Family Details
    // =============================================
    @Data
    public static class FamilyDetailsRequest {
        private String fatherName;
        private String fatherOccupation;
        private String motherName;
        private String motherOccupation;

        private FamilyDetail.FamilyType familyType;
        private FamilyDetail.FamilyValues familyValues;
        private FamilyDetail.FamilyAffluence familyAffluence;

        @Min(0) private Integer brothersCount;
        @Min(0) private Integer brothersMarried;
        @Min(0) private Integer sistersCount;
        @Min(0) private Integer sistersMarried;

        private String familyBusinessType;
        private String businessNature;
        private String businessTurnoverRange;
        private String businessLocations;
        private String aboutFamily;
    }

    // =============================================
    //  Step 3: Education Details
    // =============================================
    @Data
    public static class EducationDetailsRequest {
        @NotNull(message = "Highest qualification is required")
        private EducationDetail.Qualification highestQualification;

        private String qualificationDetail;
        private String institution;
        private String university;
        private Integer passingYear;
        private String additionalQualification;
    }

    // =============================================
    //  Step 4: Profession Details
    // =============================================
    @Data
    public static class ProfessionDetailsRequest {
        private ProfessionDetail.EmployedIn employedIn;
        private String profession;
        private String employerName;
        private String designation;
        private String annualIncomeRange;
        private String workCity;
        private String workCountry;
    }

    // =============================================
    //  Step 5: Lifestyle Details
    // =============================================
    @Data
    public static class LifestyleDetailsRequest {
        private LifestyleDetail.Diet diet;
        private LifestyleDetail.Habit smoking;
        private LifestyleDetail.Habit drinking;
        private String hobbies;
        private String interests;
        private String languagesSpoken;
    }

    // =============================================
    //  Response DTOs
    // =============================================
    @Data
    @Builder
    public static class ProfileSummaryResponse {
        private Long profileId;
        private Long userId;
        private String firstName;
        private String lastName;
        private String gender;
        private Integer age;
        private String gotra;
        private String currentCity;
        private String currentState;
        private String maritalStatus;
        private String highestQualification;
        private String profession;
        private String annualIncomeRange;
        private Integer heightCm;
        private Integer completenessScore;
        private String primaryPhotoUrl;

        public static ProfileSummaryResponse fromEntity(Profile profile, String photoBaseUrl) {
            Integer age = profile.getDateOfBirth() != null
                    ? Period.between(profile.getDateOfBirth(), LocalDate.now()).getYears()
                    : null;

            String primaryPhoto = null;
            if (profile.getPhotos() != null) {
                primaryPhoto = profile.getPhotos().stream()
                        .filter(ProfilePhoto::isPrimary)
                        .findFirst()
                        .map(p -> photoBaseUrl + "/" + p.getThumbnailKey())
                        .orElse(null);
            }

            String qualification = profile.getEducationDetail() != null
                    ? profile.getEducationDetail().getHighestQualification().name()
                    : null;

            String profession = profile.getProfessionDetail() != null
                    ? profile.getProfessionDetail().getProfession()
                    : null;

            String income = profile.getProfessionDetail() != null
                    ? profile.getProfessionDetail().getAnnualIncomeRange()
                    : null;

            return ProfileSummaryResponse.builder()
                    .profileId(profile.getId())
                    .userId(profile.getUser().getId())
                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .gender(profile.getGender().name())
                    .age(age)
                    .gotra(profile.getGotra().getDisplayName())
                    .currentCity(profile.getCurrentCity())
                    .currentState(profile.getCurrentState())
                    .maritalStatus(profile.getMaritalStatus().name())
                    .highestQualification(qualification)
                    .profession(profession)
                    .annualIncomeRange(income)
                    .heightCm(profile.getHeightCm())
                    .completenessScore(profile.getCompletenessScore())
                    .primaryPhotoUrl(primaryPhoto)
                    .build();
        }
    }

    @Data
    @Builder
    public static class ProfileDetailResponse {
        private Long profileId;
        private Long userId;

        // Personal
        private String firstName;
        private String lastName;
        private String gender;
        private LocalDate dateOfBirth;
        private Integer age;
        private Integer heightCm;
        private Integer weightKg;
        private String complexion;
        private String bloodGroup;
        private String maritalStatus;
        private String disability;

        // Community
        private String gotra;
        private String subCaste;
        private String motherGotra;

        // Location
        private String currentCity;
        private String currentState;
        private String currentCountry;
        private String nativeCity;
        private String nativeState;

        private String aboutMe;
        private String profileManagedBy;
        private Integer completenessScore;

        // Sub-sections
        private FamilyDetailResponse familyDetail;
        private EducationDetailResponse educationDetail;
        private ProfessionDetailResponse professionDetail;
        private LifestyleDetailResponse lifestyleDetail;
        private List<PhotoResponse> photos;

        public static ProfileDetailResponse fromEntity(Profile p, String photoBaseUrl) {
            Integer age = p.getDateOfBirth() != null
                    ? Period.between(p.getDateOfBirth(), LocalDate.now()).getYears()
                    : null;

            return ProfileDetailResponse.builder()
                    .profileId(p.getId())
                    .userId(p.getUser().getId())
                    .firstName(p.getFirstName())
                    .lastName(p.getLastName())
                    .gender(p.getGender().name())
                    .dateOfBirth(p.getDateOfBirth())
                    .age(age)
                    .heightCm(p.getHeightCm())
                    .weightKg(p.getWeightKg())
                    .complexion(p.getComplexion())
                    .bloodGroup(p.getBloodGroup())
                    .maritalStatus(p.getMaritalStatus().name())
                    .disability(p.getDisability())
                    .gotra(p.getGotra().getDisplayName())
                    .subCaste(p.getSubCaste())
                    .motherGotra(p.getMotherGotra())
                    .currentCity(p.getCurrentCity())
                    .currentState(p.getCurrentState())
                    .currentCountry(p.getCurrentCountry())
                    .nativeCity(p.getNativeCity())
                    .nativeState(p.getNativeState())
                    .aboutMe(p.getAboutMe())
                    .profileManagedBy(p.getProfileManagedBy() != null ? p.getProfileManagedBy().name() : null)
                    .completenessScore(p.getCompletenessScore())
                    .familyDetail(p.getFamilyDetail() != null ? FamilyDetailResponse.from(p.getFamilyDetail()) : null)
                    .educationDetail(p.getEducationDetail() != null ? EducationDetailResponse.from(p.getEducationDetail()) : null)
                    .professionDetail(p.getProfessionDetail() != null ? ProfessionDetailResponse.from(p.getProfessionDetail()) : null)
                    .lifestyleDetail(p.getLifestyleDetail() != null ? LifestyleDetailResponse.from(p.getLifestyleDetail()) : null)
                    .photos(p.getPhotos() != null ? p.getPhotos().stream()
                            .map(photo -> PhotoResponse.from(photo, photoBaseUrl)).toList() : List.of())
                    .build();
        }
    }

    @Data @Builder
    public static class FamilyDetailResponse {
        private String fatherName, fatherOccupation, motherName, motherOccupation;
        private String familyType, familyValues, familyAffluence;
        private Integer brothersCount, brothersMarried, sistersCount, sistersMarried;
        private String familyBusinessType, businessNature, businessTurnoverRange, businessLocations;
        private String aboutFamily;

        public static FamilyDetailResponse from(FamilyDetail f) {
            return FamilyDetailResponse.builder()
                    .fatherName(f.getFatherName()).fatherOccupation(f.getFatherOccupation())
                    .motherName(f.getMotherName()).motherOccupation(f.getMotherOccupation())
                    .familyType(f.getFamilyType() != null ? f.getFamilyType().name() : null)
                    .familyValues(f.getFamilyValues() != null ? f.getFamilyValues().name() : null)
                    .familyAffluence(f.getFamilyAffluence() != null ? f.getFamilyAffluence().name() : null)
                    .brothersCount(f.getBrothersCount()).brothersMarried(f.getBrothersMarried())
                    .sistersCount(f.getSistersCount()).sistersMarried(f.getSistersMarried())
                    .familyBusinessType(f.getFamilyBusinessType()).businessNature(f.getBusinessNature())
                    .businessTurnoverRange(f.getBusinessTurnoverRange()).businessLocations(f.getBusinessLocations())
                    .aboutFamily(f.getAboutFamily()).build();
        }
    }

    @Data @Builder
    public static class EducationDetailResponse {
        private String highestQualification, qualificationDetail, institution, university;
        private Integer passingYear;
        private String additionalQualification;

        public static EducationDetailResponse from(EducationDetail e) {
            return EducationDetailResponse.builder()
                    .highestQualification(e.getHighestQualification().name())
                    .qualificationDetail(e.getQualificationDetail())
                    .institution(e.getInstitution()).university(e.getUniversity())
                    .passingYear(e.getPassingYear())
                    .additionalQualification(e.getAdditionalQualification()).build();
        }
    }

    @Data @Builder
    public static class ProfessionDetailResponse {
        private String employedIn, profession, employerName, designation;
        private String annualIncomeRange, workCity, workCountry;

        public static ProfessionDetailResponse from(ProfessionDetail p) {
            return ProfessionDetailResponse.builder()
                    .employedIn(p.getEmployedIn() != null ? p.getEmployedIn().name() : null)
                    .profession(p.getProfession()).employerName(p.getEmployerName())
                    .designation(p.getDesignation()).annualIncomeRange(p.getAnnualIncomeRange())
                    .workCity(p.getWorkCity()).workCountry(p.getWorkCountry()).build();
        }
    }

    @Data @Builder
    public static class LifestyleDetailResponse {
        private String diet, smoking, drinking;
        private String hobbies, interests, languagesSpoken;

        public static LifestyleDetailResponse from(LifestyleDetail l) {
            return LifestyleDetailResponse.builder()
                    .diet(l.getDiet() != null ? l.getDiet().name() : null)
                    .smoking(l.getSmoking() != null ? l.getSmoking().name() : null)
                    .drinking(l.getDrinking() != null ? l.getDrinking().name() : null)
                    .hobbies(l.getHobbies()).interests(l.getInterests())
                    .languagesSpoken(l.getLanguagesSpoken()).build();
        }
    }

    @Data @Builder
    public static class PhotoResponse {
        private Long photoId;
        private String originalUrl;
        private String mediumUrl;
        private String thumbnailUrl;
        private Integer displayOrder;
        private boolean primary;
        private String visibility;

        public static PhotoResponse from(ProfilePhoto photo, String baseUrl) {
            return PhotoResponse.builder()
                    .photoId(photo.getId())
                    .originalUrl(baseUrl + "/" + photo.getOriginalKey())
                    .mediumUrl(photo.getMediumKey() != null ? baseUrl + "/" + photo.getMediumKey() : null)
                    .thumbnailUrl(photo.getThumbnailKey() != null ? baseUrl + "/" + photo.getThumbnailKey() : null)
                    .displayOrder(photo.getDisplayOrder())
                    .primary(photo.isPrimary())
                    .visibility(photo.getVisibility().name())
                    .build();
        }
    }
}
