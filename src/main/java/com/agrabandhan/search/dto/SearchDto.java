package com.agrabandhan.search.dto;

import com.agrabandhan.common.entity.Gotra;
import com.agrabandhan.profile.entity.EducationDetail;
import com.agrabandhan.profile.entity.LifestyleDetail;
import com.agrabandhan.profile.entity.Profile;
import com.agrabandhan.profile.entity.ProfessionDetail;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class SearchDto {

    @Data
    public static class SearchRequest {
        // Gender (opposite gender auto-applied, but allow override for admin)
        private Profile.Gender gender;

        // Age range
        private Integer ageMin;
        private Integer ageMax;

        // Height range (cm)
        private Integer heightMin;
        private Integer heightMax;

        // Gotra exclusion (auto-applied from logged-in user's gotra)
        // Additional gotras to exclude (e.g., mother's gotra)
        private List<Gotra> excludeGotras;

        // Location
        private String city;
        private String state;
        private String country;

        // Marital status
        private List<Profile.MaritalStatus> maritalStatuses;

        // Education
        private List<EducationDetail.Qualification> qualifications;

        // Profession
        private List<ProfessionDetail.EmployedIn> employedIn;
        private String incomeRange;

        // Lifestyle
        private LifestyleDetail.Diet diet;
        private LifestyleDetail.Habit smoking;
        private LifestyleDetail.Habit drinking;

        // Profile managed by
        private Profile.ProfileManagedBy managedBy;

        // Completeness filter (only show profiles above threshold)
        private Integer minCompleteness;

        // Has photo filter
        private Boolean hasPhoto;

        // Sort options
        private SortBy sortBy;
        private SortDirection sortDirection;

        // Pagination
        private Integer page;
        private Integer size;
    }

    public enum SortBy {
        RELEVANCE, NEWEST, LAST_ACTIVE, COMPLETENESS, AGE
    }

    public enum SortDirection {
        ASC, DESC
    }

    // Quick search presets
    public enum QuickSearch {
        NEW_PROFILES,       // Joined in last 7 days
        SAME_CITY,          // Same city as logged-in user
        SAME_STATE,         // Same state
        HIGH_MATCH,         // Completeness > 70
        WITH_PHOTO          // Has at least one photo
    }

    @Data
    @Builder
    public static class ProfileViewResponse {
        private Long viewId;
        private Long profileId;
        private String firstName;
        private String lastName;
        private Integer age;
        private String gotra;
        private String currentCity;
        private String primaryPhotoUrl;
        private LocalDateTime viewedAt;
    }

    @Data
    @Builder
    public static class ShortlistResponse {
        private Long shortlistId;
        private Long profileId;
        private String firstName;
        private String lastName;
        private Integer age;
        private String gotra;
        private String currentCity;
        private String primaryPhotoUrl;
        private boolean isShortlisted;
        private LocalDateTime shortlistedAt;
    }
}
