package com.agrabandhan.matching.service;

import com.agrabandhan.common.exception.ResourceNotFoundException;
import com.agrabandhan.matching.dto.MatchingDto.*;
import com.agrabandhan.matching.entity.DailyMatch;
import com.agrabandhan.matching.entity.PartnerPreference;
import com.agrabandhan.matching.repository.DailyMatchRepository;
import com.agrabandhan.matching.repository.PartnerPreferenceRepository;
import com.agrabandhan.profile.entity.*;
import com.agrabandhan.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final ProfileRepository profileRepository;
    private final PartnerPreferenceRepository preferenceRepository;
    private final DailyMatchRepository dailyMatchRepository;

    @Value("${agrabandhan.r2.public-url:}")
    private String photoBaseUrl;

    // =============================================
    //  Partner Preferences CRUD
    // =============================================
    @Transactional
    public PartnerPreferenceResponse savePreferences(Long userId, PartnerPreferenceRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        PartnerPreference pref = preferenceRepository.findByProfileId(profile.getId())
                .orElse(new PartnerPreference());

        pref.setProfile(profile);
        pref.setAgeMin(request.getAgeMin() != null ? request.getAgeMin() : 18);
        pref.setAgeMax(request.getAgeMax() != null ? request.getAgeMax() : 40);
        pref.setHeightMin(request.getHeightMin());
        pref.setHeightMax(request.getHeightMax());
        pref.setPreferredMaritalStatuses(joinList(request.getPreferredMaritalStatuses()));
        pref.setMinQualification(request.getMinQualification());
        pref.setPreferredEmployedIn(joinList(request.getPreferredEmployedIn()));
        pref.setMinIncomeRange(request.getMinIncomeRange());
        pref.setPreferredCities(joinList(request.getPreferredCities()));
        pref.setPreferredStates(joinList(request.getPreferredStates()));
        if (request.getPreferredCountry() != null) pref.setPreferredCountry(request.getPreferredCountry());
        pref.setPreferredDiet(request.getPreferredDiet());
        pref.setSmokingAcceptable(request.getSmokingAcceptable() != null ? request.getSmokingAcceptable() : false);
        pref.setDrinkingAcceptable(request.getDrinkingAcceptable() != null ? request.getDrinkingAcceptable() : false);
        pref.setPreferredFamilyType(request.getPreferredFamilyType());
        pref.setExcludeGotras(joinList(request.getExcludeGotras()));
        pref.setManglikPreference(request.getManglikPreference());

        pref = preferenceRepository.save(pref);
        return toResponse(pref);
    }

    @Transactional(readOnly = true)
    public PartnerPreferenceResponse getPreferences(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        PartnerPreference pref = preferenceRepository.findByProfileId(profile.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Partner preferences not set yet"));
        return toResponse(pref);
    }

    // =============================================
    //  Get Daily Matches
    // =============================================
    @Transactional(readOnly = true)
    public List<DailyMatchResponse> getTodayMatches(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        List<DailyMatch> matches = dailyMatchRepository.findTodayMatches(profile.getId(), LocalDate.now());

        // If no matches for today, generate on-demand
        if (matches.isEmpty()) {
            generateMatchesForProfile(profile);
            matches = dailyMatchRepository.findTodayMatches(profile.getId(), LocalDate.now());
        }

        return matches.stream().map(this::toMatchResponse).toList();
    }

    // =============================================
    //  Scheduled Daily Match Generation (6 AM IST)
    // =============================================
    @Scheduled(cron = "0 30 0 * * *", zone = "Asia/Kolkata") // 6 AM IST = 00:30 UTC
    @Transactional
    public void generateDailyMatches() {
        log.info("Starting daily match generation...");

        // Clean up matches older than 7 days
        dailyMatchRepository.deleteOldMatches(LocalDate.now().minusDays(7));

        List<Profile> activeProfiles = profileRepository.findAll().stream()
                .filter(p -> p.getUser().isActive())
                .filter(p -> p.getCompletenessScore() >= 30)
                .toList();

        int totalMatches = 0;
        for (Profile profile : activeProfiles) {
            if (!dailyMatchRepository.existsByProfileIdAndMatchDate(profile.getId(), LocalDate.now())) {
                int count = generateMatchesForProfile(profile);
                totalMatches += count;
            }
        }

        log.info("Daily match generation complete. Generated {} matches for {} profiles.",
                totalMatches, activeProfiles.size());
    }

    // =============================================
    //  Core Matching Engine
    // =============================================
    @Transactional
    public int generateMatchesForProfile(Profile profile) {
        Profile.Gender oppositeGender = profile.getGender() == Profile.Gender.MALE
                ? Profile.Gender.FEMALE : Profile.Gender.MALE;

        PartnerPreference preferences = preferenceRepository.findByProfileId(profile.getId()).orElse(null);

        // Get all potential candidates (opposite gender, different gotra, active)
        List<Profile> candidates = profileRepository.findAll().stream()
                .filter(p -> p.getGender() == oppositeGender)
                .filter(p -> p.getGotra() != profile.getGotra()) // HARD: same gotra exclusion
                .filter(p -> p.getUser().isActive())
                .filter(p -> p.getCompletenessScore() >= 20)
                .filter(p -> !p.getId().equals(profile.getId()))
                .toList();

        // Score each candidate
        List<ScoredProfile> scoredProfiles = candidates.stream()
                .map(candidate -> scoreCandidate(profile, candidate, preferences))
                .filter(sp -> sp.totalScore > 0)
                .sorted(Comparator.comparingInt(ScoredProfile::totalScore).reversed())
                .limit(10) // Top 10 matches per day
                .toList();

        // Save daily matches
        List<DailyMatch> dailyMatches = scoredProfiles.stream()
                .map(sp -> DailyMatch.builder()
                        .profile(profile)
                        .matchedProfile(sp.profile)
                        .compatibilityScore(sp.totalScore)
                        .familyScore(sp.familyScore)
                        .educationScore(sp.educationScore)
                        .locationScore(sp.locationScore)
                        .lifestyleScore(sp.lifestyleScore)
                        .preferenceScore(sp.preferenceScore)
                        .matchDate(LocalDate.now())
                        .build())
                .toList();

        dailyMatchRepository.saveAll(dailyMatches);
        return dailyMatches.size();
    }

    private ScoredProfile scoreCandidate(Profile seeker, Profile candidate, PartnerPreference prefs) {
        int familyScore = scoreFamilyBackground(seeker, candidate);       // Max 25
        int educationScore = scoreEducation(seeker, candidate);           // Max 20
        int locationScore = scoreLocation(seeker, candidate);             // Max 15
        int lifestyleScore = scoreLifestyle(seeker, candidate);           // Max 10
        int preferenceScore = scoreAgainstPreferences(candidate, prefs);  // Max 30

        int total = familyScore + educationScore + locationScore + lifestyleScore + preferenceScore;

        return new ScoredProfile(candidate, total, familyScore, educationScore,
                locationScore, lifestyleScore, preferenceScore);
    }

    private int scoreFamilyBackground(Profile seeker, Profile candidate) {
        int score = 0;
        FamilyDetail seekerFamily = seeker.getFamilyDetail();
        FamilyDetail candidateFamily = candidate.getFamilyDetail();

        if (seekerFamily == null || candidateFamily == null) return 5; // Base score

        // Family type match
        if (seekerFamily.getFamilyType() != null && candidateFamily.getFamilyType() != null
                && seekerFamily.getFamilyType() == candidateFamily.getFamilyType()) {
            score += 8;
        }

        // Family values match
        if (seekerFamily.getFamilyValues() != null && candidateFamily.getFamilyValues() != null
                && seekerFamily.getFamilyValues() == candidateFamily.getFamilyValues()) {
            score += 8;
        }

        // Family affluence proximity
        if (seekerFamily.getFamilyAffluence() != null && candidateFamily.getFamilyAffluence() != null) {
            int diff = Math.abs(seekerFamily.getFamilyAffluence().ordinal() - candidateFamily.getFamilyAffluence().ordinal());
            score += Math.max(0, 9 - diff * 3);
        }

        return Math.min(25, score);
    }

    private int scoreEducation(Profile seeker, Profile candidate) {
        int score = 0;
        EducationDetail candidateEdu = candidate.getEducationDetail();

        if (candidateEdu == null) return 3;

        // Higher education = higher score
        int qualOrdinal = candidateEdu.getHighestQualification().ordinal();
        score += Math.min(15, qualOrdinal * 3);

        // Bonus for institution filled
        if (candidateEdu.getInstitution() != null) score += 5;

        return Math.min(20, score);
    }

    private int scoreLocation(Profile seeker, Profile candidate) {
        int score = 0;

        // Same city = full score
        if (seeker.getCurrentCity() != null && candidate.getCurrentCity() != null
                && seeker.getCurrentCity().equalsIgnoreCase(candidate.getCurrentCity())) {
            return 15;
        }

        // Same state = partial
        if (seeker.getCurrentState() != null && candidate.getCurrentState() != null
                && seeker.getCurrentState().equalsIgnoreCase(candidate.getCurrentState())) {
            score += 10;
        }

        // Same country
        if (seeker.getCurrentCountry() != null && candidate.getCurrentCountry() != null
                && seeker.getCurrentCountry().equalsIgnoreCase(candidate.getCurrentCountry())) {
            score += 5;
        }

        return Math.min(15, score);
    }

    private int scoreLifestyle(Profile seeker, Profile candidate) {
        int score = 0;
        LifestyleDetail seekerLife = seeker.getLifestyleDetail();
        LifestyleDetail candidateLife = candidate.getLifestyleDetail();

        if (seekerLife == null || candidateLife == null) return 3;

        // Diet match
        if (seekerLife.getDiet() != null && candidateLife.getDiet() != null
                && seekerLife.getDiet() == candidateLife.getDiet()) {
            score += 5;
        }

        // Smoking/drinking alignment
        if (seekerLife.getSmoking() == candidateLife.getSmoking()) score += 2;
        if (seekerLife.getDrinking() == candidateLife.getDrinking()) score += 3;

        return Math.min(10, score);
    }

    private int scoreAgainstPreferences(Profile candidate, PartnerPreference prefs) {
        if (prefs == null) return 15; // Default mid-score if no preferences set

        int score = 0;
        int maxScore = 30;

        // Age check
        if (candidate.getDateOfBirth() != null) {
            int age = Period.between(candidate.getDateOfBirth(), LocalDate.now()).getYears();
            if (age >= prefs.getAgeMin() && age <= prefs.getAgeMax()) {
                score += 8;
            }
        }

        // Height check
        if (candidate.getHeightCm() != null) {
            boolean heightOk = true;
            if (prefs.getHeightMin() != null && candidate.getHeightCm() < prefs.getHeightMin()) heightOk = false;
            if (prefs.getHeightMax() != null && candidate.getHeightCm() > prefs.getHeightMax()) heightOk = false;
            if (heightOk) score += 4;
        }

        // Marital status match
        if (prefs.getPreferredMaritalStatuses() != null && !prefs.getPreferredMaritalStatuses().isBlank()) {
            List<String> preferred = splitList(prefs.getPreferredMaritalStatuses());
            if (preferred.contains(candidate.getMaritalStatus().name())) {
                score += 5;
            }
        } else {
            score += 5; // No preference = accepts all
        }

        // Location match
        if (prefs.getPreferredCities() != null && !prefs.getPreferredCities().isBlank()) {
            List<String> cities = splitList(prefs.getPreferredCities());
            if (candidate.getCurrentCity() != null && cities.stream()
                    .anyMatch(c -> c.equalsIgnoreCase(candidate.getCurrentCity()))) {
                score += 5;
            }
        } else {
            score += 3;
        }

        // Diet match
        if (prefs.getPreferredDiet() != null && candidate.getLifestyleDetail() != null
                && candidate.getLifestyleDetail().getDiet() != null
                && prefs.getPreferredDiet().equalsIgnoreCase(candidate.getLifestyleDetail().getDiet().name())) {
            score += 4;
        }

        // Smoking/Drinking check
        if (candidate.getLifestyleDetail() != null) {
            if (Boolean.TRUE.equals(prefs.getSmokingAcceptable())
                    || candidate.getLifestyleDetail().getSmoking() == LifestyleDetail.Habit.NO) {
                score += 2;
            }
            if (Boolean.TRUE.equals(prefs.getDrinkingAcceptable())
                    || candidate.getLifestyleDetail().getDrinking() == LifestyleDetail.Habit.NO) {
                score += 2;
            }
        }

        return Math.min(maxScore, score);
    }

    // =============================================
    //  Helpers
    // =============================================
    private String joinList(List<String> list) {
        return list != null && !list.isEmpty() ? String.join(",", list) : null;
    }

    private List<String> splitList(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(",")).map(String::trim).toList();
    }

    private PartnerPreferenceResponse toResponse(PartnerPreference p) {
        return PartnerPreferenceResponse.builder()
                .id(p.getId())
                .ageMin(p.getAgeMin()).ageMax(p.getAgeMax())
                .heightMin(p.getHeightMin()).heightMax(p.getHeightMax())
                .preferredMaritalStatuses(splitList(p.getPreferredMaritalStatuses()))
                .minQualification(p.getMinQualification())
                .preferredEmployedIn(splitList(p.getPreferredEmployedIn()))
                .minIncomeRange(p.getMinIncomeRange())
                .preferredCities(splitList(p.getPreferredCities()))
                .preferredStates(splitList(p.getPreferredStates()))
                .preferredCountry(p.getPreferredCountry())
                .preferredDiet(p.getPreferredDiet())
                .smokingAcceptable(p.getSmokingAcceptable())
                .drinkingAcceptable(p.getDrinkingAcceptable())
                .preferredFamilyType(p.getPreferredFamilyType())
                .excludeGotras(splitList(p.getExcludeGotras()))
                .manglikPreference(p.getManglikPreference() != null ? p.getManglikPreference().name() : null)
                .build();
    }

    private DailyMatchResponse toMatchResponse(DailyMatch dm) {
        Profile mp = dm.getMatchedProfile();
        Integer age = mp.getDateOfBirth() != null
                ? Period.between(mp.getDateOfBirth(), LocalDate.now()).getYears() : null;

        String photo = mp.getPhotos() != null ? mp.getPhotos().stream()
                .filter(ProfilePhoto::isPrimary).findFirst()
                .map(p -> photoBaseUrl + "/" + p.getThumbnailKey())
                .orElse(null) : null;

        String qual = mp.getEducationDetail() != null
                ? mp.getEducationDetail().getHighestQualification().name() : null;
        String prof = mp.getProfessionDetail() != null
                ? mp.getProfessionDetail().getProfession() : null;

        return DailyMatchResponse.builder()
                .matchId(dm.getId())
                .profileId(mp.getId())
                .firstName(mp.getFirstName()).lastName(mp.getLastName())
                .age(age).gotra(mp.getGotra().getDisplayName())
                .currentCity(mp.getCurrentCity())
                .primaryPhotoUrl(photo)
                .qualification(qual).profession(prof)
                .compatibilityScore(dm.getCompatibilityScore())
                .matchDate(dm.getMatchDate())
                .familyScore(dm.getFamilyScore())
                .educationScore(dm.getEducationScore())
                .locationScore(dm.getLocationScore())
                .lifestyleScore(dm.getLifestyleScore())
                .preferenceScore(dm.getPreferenceScore())
                .build();
    }

    private record ScoredProfile(Profile profile, int totalScore, int familyScore,
                                  int educationScore, int locationScore,
                                  int lifestyleScore, int preferenceScore) {}
}
