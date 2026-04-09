package com.agrabandhan.profile.service;

import com.agrabandhan.auth.entity.User;
import com.agrabandhan.auth.repository.UserRepository;
import com.agrabandhan.common.exception.BadRequestException;
import com.agrabandhan.common.exception.DuplicateResourceException;
import com.agrabandhan.common.exception.ResourceNotFoundException;
import com.agrabandhan.profile.dto.ProfileDto.*;
import com.agrabandhan.profile.entity.*;
import com.agrabandhan.profile.repository.ProfilePhotoRepository;
import com.agrabandhan.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final ProfilePhotoRepository photoRepository;
    private final UserRepository userRepository;

    @Value("${agrabandhan.r2.public-url:}")
    private String photoBaseUrl;

    // =============================================
    //  Step 1: Create profile with personal details
    // =============================================
    @Transactional
    public ProfileDetailResponse createPersonalDetails(Long userId, PersonalDetailsRequest request) {
        if (profileRepository.existsByUserId(userId)) {
            throw new DuplicateResourceException("Profile already exists for this user. Use update instead.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Validate age (minimum 18)
        if (request.getDateOfBirth() != null) {
            int age = java.time.Period.between(request.getDateOfBirth(), java.time.LocalDate.now()).getYears();
            if (age < 18) {
                throw new BadRequestException("User must be at least 18 years old");
            }
            if (age > 70) {
                throw new BadRequestException("Invalid date of birth");
            }
        }

        Profile profile = Profile.builder()
                .user(user)
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .gender(request.getGender())
                .dateOfBirth(request.getDateOfBirth())
                .heightCm(request.getHeightCm())
                .weightKg(request.getWeightKg())
                .complexion(request.getComplexion())
                .bloodGroup(request.getBloodGroup())
                .maritalStatus(request.getMaritalStatus())
                .disability(request.getDisability())
                .gotra(request.getGotra())
                .subCaste(request.getSubCaste())
                .motherGotra(request.getMotherGotra())
                .currentCity(request.getCurrentCity())
                .currentState(request.getCurrentState())
                .currentCountry(request.getCurrentCountry() != null ? request.getCurrentCountry() : "India")
                .nativeCity(request.getNativeCity())
                .nativeState(request.getNativeState())
                .aboutMe(request.getAboutMe())
                .profileManagedBy(request.getProfileManagedBy() != null ? request.getProfileManagedBy() : Profile.ProfileManagedBy.SELF)
                .build();

        profile = profileRepository.save(profile);
        updateCompletenessScore(profile);

        // Mark profile creation started on user
        user.setProfileComplete(false);
        userRepository.save(user);

        log.info("Profile created for userId: {}", userId);
        return ProfileDetailResponse.fromEntity(profile, photoBaseUrl);
    }

    // =============================================
    //  Step 1: Update personal details
    // =============================================
    @Transactional
    public ProfileDetailResponse updatePersonalDetails(Long userId, PersonalDetailsRequest request) {
        Profile profile = getProfileByUserId(userId);

        profile.setFirstName(request.getFirstName());
        profile.setLastName(request.getLastName());
        profile.setGender(request.getGender());
        profile.setDateOfBirth(request.getDateOfBirth());
        profile.setHeightCm(request.getHeightCm());
        profile.setWeightKg(request.getWeightKg());
        profile.setComplexion(request.getComplexion());
        profile.setBloodGroup(request.getBloodGroup());
        profile.setMaritalStatus(request.getMaritalStatus());
        profile.setDisability(request.getDisability());
        profile.setGotra(request.getGotra());
        profile.setSubCaste(request.getSubCaste());
        profile.setMotherGotra(request.getMotherGotra());
        profile.setCurrentCity(request.getCurrentCity());
        profile.setCurrentState(request.getCurrentState());
        if (request.getCurrentCountry() != null) profile.setCurrentCountry(request.getCurrentCountry());
        profile.setNativeCity(request.getNativeCity());
        profile.setNativeState(request.getNativeState());
        profile.setAboutMe(request.getAboutMe());
        if (request.getProfileManagedBy() != null) profile.setProfileManagedBy(request.getProfileManagedBy());

        profile = profileRepository.save(profile);
        updateCompletenessScore(profile);

        return ProfileDetailResponse.fromEntity(profile, photoBaseUrl);
    }

    // =============================================
    //  Step 2: Save/Update family details
    // =============================================
    @Transactional
    public ProfileDetailResponse saveFamilyDetails(Long userId, FamilyDetailsRequest request) {
        Profile profile = getProfileByUserId(userId);

        FamilyDetail family = profile.getFamilyDetail();
        if (family == null) {
            family = new FamilyDetail();
            family.setProfile(profile);
        }

        family.setFatherName(request.getFatherName());
        family.setFatherOccupation(request.getFatherOccupation());
        family.setMotherName(request.getMotherName());
        family.setMotherOccupation(request.getMotherOccupation());
        family.setFamilyType(request.getFamilyType());
        family.setFamilyValues(request.getFamilyValues());
        family.setFamilyAffluence(request.getFamilyAffluence());
        family.setBrothersCount(request.getBrothersCount() != null ? request.getBrothersCount() : 0);
        family.setBrothersMarried(request.getBrothersMarried() != null ? request.getBrothersMarried() : 0);
        family.setSistersCount(request.getSistersCount() != null ? request.getSistersCount() : 0);
        family.setSistersMarried(request.getSistersMarried() != null ? request.getSistersMarried() : 0);
        family.setFamilyBusinessType(request.getFamilyBusinessType());
        family.setBusinessNature(request.getBusinessNature());
        family.setBusinessTurnoverRange(request.getBusinessTurnoverRange());
        family.setBusinessLocations(request.getBusinessLocations());
        family.setAboutFamily(request.getAboutFamily());

        profile.setFamilyDetail(family);
        profile = profileRepository.save(profile);
        updateCompletenessScore(profile);

        return ProfileDetailResponse.fromEntity(profile, photoBaseUrl);
    }

    // =============================================
    //  Step 3: Save/Update education details
    // =============================================
    @Transactional
    public ProfileDetailResponse saveEducationDetails(Long userId, EducationDetailsRequest request) {
        Profile profile = getProfileByUserId(userId);

        EducationDetail education = profile.getEducationDetail();
        if (education == null) {
            education = new EducationDetail();
            education.setProfile(profile);
        }

        education.setHighestQualification(request.getHighestQualification());
        education.setQualificationDetail(request.getQualificationDetail());
        education.setInstitution(request.getInstitution());
        education.setUniversity(request.getUniversity());
        education.setPassingYear(request.getPassingYear());
        education.setAdditionalQualification(request.getAdditionalQualification());

        profile.setEducationDetail(education);
        profile = profileRepository.save(profile);
        updateCompletenessScore(profile);

        return ProfileDetailResponse.fromEntity(profile, photoBaseUrl);
    }

    // =============================================
    //  Step 4: Save/Update profession details
    // =============================================
    @Transactional
    public ProfileDetailResponse saveProfessionDetails(Long userId, ProfessionDetailsRequest request) {
        Profile profile = getProfileByUserId(userId);

        ProfessionDetail profession = profile.getProfessionDetail();
        if (profession == null) {
            profession = new ProfessionDetail();
            profession.setProfile(profile);
        }

        profession.setEmployedIn(request.getEmployedIn());
        profession.setProfession(request.getProfession());
        profession.setEmployerName(request.getEmployerName());
        profession.setDesignation(request.getDesignation());
        profession.setAnnualIncomeRange(request.getAnnualIncomeRange());
        profession.setWorkCity(request.getWorkCity());
        if (request.getWorkCountry() != null) profession.setWorkCountry(request.getWorkCountry());

        profile.setProfessionDetail(profession);
        profile = profileRepository.save(profile);
        updateCompletenessScore(profile);

        return ProfileDetailResponse.fromEntity(profile, photoBaseUrl);
    }

    // =============================================
    //  Step 5: Save/Update lifestyle details
    // =============================================
    @Transactional
    public ProfileDetailResponse saveLifestyleDetails(Long userId, LifestyleDetailsRequest request) {
        Profile profile = getProfileByUserId(userId);

        LifestyleDetail lifestyle = profile.getLifestyleDetail();
        if (lifestyle == null) {
            lifestyle = new LifestyleDetail();
            lifestyle.setProfile(profile);
        }

        lifestyle.setDiet(request.getDiet());
        lifestyle.setSmoking(request.getSmoking());
        lifestyle.setDrinking(request.getDrinking());
        lifestyle.setHobbies(request.getHobbies());
        lifestyle.setInterests(request.getInterests());
        lifestyle.setLanguagesSpoken(request.getLanguagesSpoken());

        profile.setLifestyleDetail(lifestyle);
        profile = profileRepository.save(profile);
        updateCompletenessScore(profile);

        // Check if profile is now complete enough
        if (profile.getCompletenessScore() >= 60) {
            User user = profile.getUser();
            user.setProfileComplete(true);
            userRepository.save(user);
        }

        return ProfileDetailResponse.fromEntity(profile, photoBaseUrl);
    }

    // =============================================
    //  Get profile
    // =============================================
    @Transactional(readOnly = true)
    public ProfileDetailResponse getMyProfile(Long userId) {
        Profile profile = profileRepository.findByUserIdWithAllDetails(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));
        return ProfileDetailResponse.fromEntity(profile, photoBaseUrl);
    }

    @Transactional(readOnly = true)
    public ProfileDetailResponse getProfileById(Long profileId) {
        Profile profile = profileRepository.findByIdWithAllDetails(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", profileId));
        return ProfileDetailResponse.fromEntity(profile, photoBaseUrl);
    }

    // =============================================
    //  Completeness score calculation
    // =============================================
    private void updateCompletenessScore(Profile profile) {
        int score = 0;

        // Personal details (30 points)
        if (profile.getFirstName() != null) score += 5;
        if (profile.getLastName() != null) score += 5;
        if (profile.getDateOfBirth() != null) score += 5;
        if (profile.getGotra() != null) score += 5;
        if (profile.getCurrentCity() != null) score += 5;
        if (profile.getAboutMe() != null && profile.getAboutMe().length() > 20) score += 5;

        // Family details (20 points)
        if (profile.getFamilyDetail() != null) {
            FamilyDetail f = profile.getFamilyDetail();
            if (f.getFatherName() != null) score += 5;
            if (f.getMotherName() != null) score += 5;
            if (f.getFamilyType() != null) score += 5;
            if (f.getFamilyBusinessType() != null || f.getAboutFamily() != null) score += 5;
        }

        // Education (15 points)
        if (profile.getEducationDetail() != null) {
            EducationDetail e = profile.getEducationDetail();
            if (e.getHighestQualification() != null) score += 10;
            if (e.getInstitution() != null) score += 5;
        }

        // Profession (15 points)
        if (profile.getProfessionDetail() != null) {
            ProfessionDetail p = profile.getProfessionDetail();
            if (p.getEmployedIn() != null) score += 5;
            if (p.getProfession() != null) score += 5;
            if (p.getAnnualIncomeRange() != null) score += 5;
        }

        // Lifestyle (10 points)
        if (profile.getLifestyleDetail() != null) {
            LifestyleDetail l = profile.getLifestyleDetail();
            if (l.getDiet() != null) score += 5;
            if (l.getLanguagesSpoken() != null) score += 5;
        }

        // Photos (10 points)
        if (profile.getPhotos() != null && !profile.getPhotos().isEmpty()) {
            score += Math.min(10, profile.getPhotos().size() * 5);
        }

        profile.setCompletenessScore(Math.min(100, score));
        profileRepository.save(profile);
    }

    // =============================================
    //  Helper
    // =============================================
    private Profile getProfileByUserId(Long userId) {
        return profileRepository.findByUserIdWithAllDetails(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));
    }
}
