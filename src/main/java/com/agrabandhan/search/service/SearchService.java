package com.agrabandhan.search.service;

import com.agrabandhan.common.dto.PagedResponse;
import com.agrabandhan.common.entity.Gotra;
import com.agrabandhan.common.exception.ResourceNotFoundException;
import com.agrabandhan.profile.dto.ProfileDto.ProfileSummaryResponse;
import com.agrabandhan.profile.entity.*;
import com.agrabandhan.profile.repository.ProfileRepository;
import com.agrabandhan.search.dto.SearchDto.*;
import com.agrabandhan.search.entity.ProfileView;
import com.agrabandhan.search.entity.ShortlistedProfile;
import com.agrabandhan.search.repository.ProfileViewRepository;
import com.agrabandhan.search.repository.ShortlistedProfileRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final EntityManager entityManager;
    private final ProfileRepository profileRepository;
    private final ProfileViewRepository profileViewRepository;
    private final ShortlistedProfileRepository shortlistRepository;

    @Value("${agrabandhan.r2.public-url:}")
    private String photoBaseUrl;

    // =============================================
    //  Advanced Search with 20+ filters
    // =============================================
    @Transactional(readOnly = true)
    public PagedResponse<ProfileSummaryResponse> search(Long userId, SearchRequest request) {
        Profile myProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        int page = request.getPage() != null ? request.getPage() : 0;
        int size = request.getSize() != null ? Math.min(request.getSize(), 50) : 20;

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();

        // --- Count query ---
        CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
        Root<Profile> countRoot = countQuery.from(Profile.class);
        List<Predicate> countPredicates = buildPredicates(cb, countRoot, myProfile, request);
        countQuery.select(cb.count(countRoot)).where(countPredicates.toArray(new Predicate[0]));
        Long totalElements = entityManager.createQuery(countQuery).getSingleResult();

        // --- Data query ---
        CriteriaQuery<Profile> dataQuery = cb.createQuery(Profile.class);
        Root<Profile> root = dataQuery.from(Profile.class);
        root.fetch("user", JoinType.LEFT);

        List<Predicate> predicates = buildPredicates(cb, root, myProfile, request);
        dataQuery.where(predicates.toArray(new Predicate[0]));

        // Sorting
        applySorting(cb, dataQuery, root, request);

        TypedQuery<Profile> typedQuery = entityManager.createQuery(dataQuery);
        typedQuery.setFirstResult(page * size);
        typedQuery.setMaxResults(size);

        List<Profile> profiles = typedQuery.getResultList();

        List<ProfileSummaryResponse> content = profiles.stream()
                .map(p -> ProfileSummaryResponse.fromEntity(p, photoBaseUrl))
                .toList();

        Page<ProfileSummaryResponse> resultPage = new PageImpl<>(content,
                PageRequest.of(page, size), totalElements);

        return PagedResponse.from(resultPage);
    }

    // =============================================
    //  Quick Search Presets
    // =============================================
    @Transactional(readOnly = true)
    public PagedResponse<ProfileSummaryResponse> quickSearch(Long userId, QuickSearch preset, int page, int size) {
        SearchRequest request = new SearchRequest();
        request.setPage(page);
        request.setSize(size);
        request.setSortDirection(SortDirection.DESC);

        Profile myProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        switch (preset) {
            case NEW_PROFILES -> request.setSortBy(SortBy.NEWEST);
            case SAME_CITY -> {
                request.setCity(myProfile.getCurrentCity());
                request.setSortBy(SortBy.NEWEST);
            }
            case SAME_STATE -> {
                request.setState(myProfile.getCurrentState());
                request.setSortBy(SortBy.NEWEST);
            }
            case HIGH_MATCH -> {
                request.setMinCompleteness(70);
                request.setSortBy(SortBy.COMPLETENESS);
            }
            case WITH_PHOTO -> {
                request.setHasPhoto(true);
                request.setSortBy(SortBy.NEWEST);
            }
        }

        return search(userId, request);
    }

    // =============================================
    //  Profile View Tracking
    // =============================================
    @Transactional
    public void recordProfileView(Long viewerUserId, Long viewedProfileId) {
        Profile viewerProfile = profileRepository.findByUserId(viewerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", viewerUserId));
        Profile viewedProfile = profileRepository.findById(viewedProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", viewedProfileId));

        // Don't record self-views
        if (viewerProfile.getId().equals(viewedProfile.getId())) return;

        // Upsert: update timestamp if already viewed, otherwise insert
        profileViewRepository.findByViewerProfileIdAndViewedProfileId(
                viewerProfile.getId(), viewedProfile.getId())
                .ifPresentOrElse(
                        existing -> existing.setViewedAt(LocalDateTime.now()),
                        () -> profileViewRepository.save(ProfileView.builder()
                                .viewerProfile(viewerProfile)
                                .viewedProfile(viewedProfile)
                                .build())
                );
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProfileViewResponse> getWhoViewedMe(Long userId, int page, int size) {
        Profile myProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<ProfileView> views = profileViewRepository.findWhoViewedMe(myProfile.getId(), pageable);

        Page<ProfileViewResponse> responsePage = views.map(v -> toViewResponse(v.getViewerProfile(), v.getViewedAt()));
        return PagedResponse.from(responsePage);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ProfileViewResponse> getMyViewHistory(Long userId, int page, int size) {
        Profile myProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<ProfileView> views = profileViewRepository.findMyViews(myProfile.getId(), pageable);

        Page<ProfileViewResponse> responsePage = views.map(v -> toViewResponse(v.getViewedProfile(), v.getViewedAt()));
        return PagedResponse.from(responsePage);
    }

    // =============================================
    //  Shortlist
    // =============================================
    @Transactional
    public ShortlistResponse addToShortlist(Long userId, Long targetProfileId) {
        Profile myProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));
        Profile targetProfile = profileRepository.findById(targetProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", targetProfileId));

        if (myProfile.getId().equals(targetProfile.getId())) {
            throw new IllegalArgumentException("Cannot shortlist your own profile");
        }

        if (shortlistRepository.existsByProfileIdAndShortlistedProfileId(myProfile.getId(), targetProfile.getId())) {
            throw new IllegalArgumentException("Profile already shortlisted");
        }

        ShortlistedProfile shortlisted = shortlistRepository.save(ShortlistedProfile.builder()
                .profile(myProfile)
                .shortlistedProfile(targetProfile)
                .build());

        return toShortlistResponse(targetProfile, shortlisted.getCreatedAt(), true);
    }

    @Transactional
    public void removeFromShortlist(Long userId, Long targetProfileId) {
        Profile myProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        shortlistRepository.deleteByProfileIdAndShortlistedProfileId(myProfile.getId(), targetProfileId);
    }

    @Transactional(readOnly = true)
    public PagedResponse<ShortlistResponse> getMyShortlist(Long userId, int page, int size) {
        Profile myProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        Pageable pageable = PageRequest.of(page, Math.min(size, 50));
        Page<ShortlistedProfile> shortlisted = shortlistRepository.findMyShortlist(myProfile.getId(), pageable);

        Page<ShortlistResponse> responsePage = shortlisted.map(s ->
                toShortlistResponse(s.getShortlistedProfile(), s.getCreatedAt(), true));
        return PagedResponse.from(responsePage);
    }

    // =============================================
    //  Private: Build search predicates
    // =============================================
    private List<Predicate> buildPredicates(CriteriaBuilder cb, Root<Profile> root,
                                             Profile myProfile, SearchRequest request) {
        List<Predicate> predicates = new ArrayList<>();

        // --- HARD RULES (always applied) ---

        // Exclude self
        predicates.add(cb.notEqual(root.get("id"), myProfile.getId()));

        // Exclude same gotra (CRITICAL - never show same gotra)
        predicates.add(cb.notEqual(root.get("gotra"), myProfile.getGotra()));

        // Exclude additional gotras (e.g., mother's gotra)
        if (request.getExcludeGotras() != null && !request.getExcludeGotras().isEmpty()) {
            predicates.add(root.get("gotra").in(request.getExcludeGotras()).not());
        }

        // Opposite gender by default
        if (request.getGender() != null) {
            predicates.add(cb.equal(root.get("gender"), request.getGender()));
        } else {
            Profile.Gender oppositeGender = myProfile.getGender() == Profile.Gender.MALE
                    ? Profile.Gender.FEMALE : Profile.Gender.MALE;
            predicates.add(cb.equal(root.get("gender"), oppositeGender));
        }

        // Only active users
        predicates.add(cb.isTrue(root.get("user").get("active")));

        // --- SOFT FILTERS (applied if provided) ---

        // Age range (convert to date of birth range)
        if (request.getAgeMin() != null) {
            LocalDate maxDob = LocalDate.now().minusYears(request.getAgeMin());
            predicates.add(cb.lessThanOrEqualTo(root.get("dateOfBirth"), maxDob));
        }
        if (request.getAgeMax() != null) {
            LocalDate minDob = LocalDate.now().minusYears(request.getAgeMax() + 1);
            predicates.add(cb.greaterThan(root.get("dateOfBirth"), minDob));
        }

        // Height range
        if (request.getHeightMin() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("heightCm"), request.getHeightMin()));
        }
        if (request.getHeightMax() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("heightCm"), request.getHeightMax()));
        }

        // Location
        if (request.getCity() != null && !request.getCity().isBlank()) {
            predicates.add(cb.equal(cb.lower(root.get("currentCity")), request.getCity().toLowerCase()));
        }
        if (request.getState() != null && !request.getState().isBlank()) {
            predicates.add(cb.equal(cb.lower(root.get("currentState")), request.getState().toLowerCase()));
        }
        if (request.getCountry() != null && !request.getCountry().isBlank()) {
            predicates.add(cb.equal(cb.lower(root.get("currentCountry")), request.getCountry().toLowerCase()));
        }

        // Marital status
        if (request.getMaritalStatuses() != null && !request.getMaritalStatuses().isEmpty()) {
            predicates.add(root.get("maritalStatus").in(request.getMaritalStatuses()));
        }

        // Education (requires join)
        if (request.getQualifications() != null && !request.getQualifications().isEmpty()) {
            Join<Profile, EducationDetail> eduJoin = root.join("educationDetail", JoinType.LEFT);
            predicates.add(eduJoin.get("highestQualification").in(request.getQualifications()));
        }

        // Profession (requires join)
        if (request.getEmployedIn() != null && !request.getEmployedIn().isEmpty()) {
            Join<Profile, ProfessionDetail> profJoin = root.join("professionDetail", JoinType.LEFT);
            predicates.add(profJoin.get("employedIn").in(request.getEmployedIn()));
        }
        if (request.getIncomeRange() != null && !request.getIncomeRange().isBlank()) {
            Join<Profile, ProfessionDetail> profJoin = root.join("professionDetail", JoinType.LEFT);
            predicates.add(cb.equal(profJoin.get("annualIncomeRange"), request.getIncomeRange()));
        }

        // Lifestyle (requires join)
        if (request.getDiet() != null) {
            Join<Profile, LifestyleDetail> lifeJoin = root.join("lifestyleDetail", JoinType.LEFT);
            predicates.add(cb.equal(lifeJoin.get("diet"), request.getDiet()));
        }
        if (request.getSmoking() != null) {
            Join<Profile, LifestyleDetail> lifeJoin = root.join("lifestyleDetail", JoinType.LEFT);
            predicates.add(cb.equal(lifeJoin.get("smoking"), request.getSmoking()));
        }
        if (request.getDrinking() != null) {
            Join<Profile, LifestyleDetail> lifeJoin = root.join("lifestyleDetail", JoinType.LEFT);
            predicates.add(cb.equal(lifeJoin.get("drinking"), request.getDrinking()));
        }

        // Profile managed by
        if (request.getManagedBy() != null) {
            predicates.add(cb.equal(root.get("profileManagedBy"), request.getManagedBy()));
        }

        // Minimum completeness
        if (request.getMinCompleteness() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("completenessScore"), request.getMinCompleteness()));
        }

        // Has photo
        if (Boolean.TRUE.equals(request.getHasPhoto())) {
            Subquery<Long> photoSubquery = ((CriteriaQuery<?>) root.getParentPath()).subquery(Long.class);
            Root<ProfilePhoto> photoRoot = photoSubquery.from(ProfilePhoto.class);
            photoSubquery.select(cb.count(photoRoot))
                    .where(cb.equal(photoRoot.get("profile").get("id"), root.get("id")));
            predicates.add(cb.greaterThan(photoSubquery, 0L));
        }

        return predicates;
    }

    private void applySorting(CriteriaBuilder cb, CriteriaQuery<Profile> query,
                               Root<Profile> root, SearchRequest request) {
        SortBy sortBy = request.getSortBy() != null ? request.getSortBy() : SortBy.NEWEST;
        boolean isDesc = request.getSortDirection() != SortDirection.ASC;

        Order order = switch (sortBy) {
            case NEWEST -> isDesc ? cb.desc(root.get("createdAt")) : cb.asc(root.get("createdAt"));
            case AGE -> isDesc ? cb.desc(root.get("dateOfBirth")) : cb.asc(root.get("dateOfBirth"));
            case COMPLETENESS -> isDesc ? cb.desc(root.get("completenessScore")) : cb.asc(root.get("completenessScore"));
            default -> cb.desc(root.get("completenessScore")); // RELEVANCE = completeness for now
        };

        query.orderBy(order);
    }

    // =============================================
    //  Response mappers
    // =============================================
    private ProfileViewResponse toViewResponse(Profile profile, LocalDateTime viewedAt) {
        Integer age = profile.getDateOfBirth() != null
                ? Period.between(profile.getDateOfBirth(), LocalDate.now()).getYears() : null;

        String photo = profile.getPhotos() != null ? profile.getPhotos().stream()
                .filter(ProfilePhoto::isPrimary).findFirst()
                .map(p -> photoBaseUrl + "/" + p.getThumbnailKey())
                .orElse(null) : null;

        return ProfileViewResponse.builder()
                .profileId(profile.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .age(age)
                .gotra(profile.getGotra().getDisplayName())
                .currentCity(profile.getCurrentCity())
                .primaryPhotoUrl(photo)
                .viewedAt(viewedAt)
                .build();
    }

    private ShortlistResponse toShortlistResponse(Profile profile, LocalDateTime at, boolean isShortlisted) {
        Integer age = profile.getDateOfBirth() != null
                ? Period.between(profile.getDateOfBirth(), LocalDate.now()).getYears() : null;

        String photo = profile.getPhotos() != null ? profile.getPhotos().stream()
                .filter(ProfilePhoto::isPrimary).findFirst()
                .map(p -> photoBaseUrl + "/" + p.getThumbnailKey())
                .orElse(null) : null;

        return ShortlistResponse.builder()
                .profileId(profile.getId())
                .firstName(profile.getFirstName())
                .lastName(profile.getLastName())
                .age(age)
                .gotra(profile.getGotra().getDisplayName())
                .currentCity(profile.getCurrentCity())
                .primaryPhotoUrl(photo)
                .isShortlisted(isShortlisted)
                .shortlistedAt(at)
                .build();
    }
}
