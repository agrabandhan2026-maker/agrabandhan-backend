package com.agrabandhan.community.service;

import com.agrabandhan.auth.entity.User;
import com.agrabandhan.auth.repository.UserRepository;
import com.agrabandhan.common.exception.BadRequestException;
import com.agrabandhan.common.exception.ResourceNotFoundException;
import com.agrabandhan.common.util.StorageService;
import com.agrabandhan.community.dto.CommunityDto.*;
import com.agrabandhan.community.entity.*;
import com.agrabandhan.community.entity.VerificationRequest.DocumentType;
import com.agrabandhan.community.entity.VerificationRequest.VerificationStatus;
import com.agrabandhan.community.repository.*;
import com.agrabandhan.communication.entity.Interest;
import com.agrabandhan.communication.repository.InterestRepository;
import com.agrabandhan.profile.entity.Profile;
import com.agrabandhan.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommunityService {

    private final VerificationRequestRepository verificationRepo;
    private final PhotoVerificationRepository photoVerificationRepo;
    private final EndorsementRepository endorsementRepo;
    private final SabhaRepository sabhaRepo;
    private final ProfileSabhaLinkRepository sabhaLinkRepo;
    private final TrustScoreRepository trustScoreRepo;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Value("${agrabandhan.r2.public-url:}")
    private String publicUrl;

    // =============================================
    //  ID Verification
    // =============================================
    @Transactional
    public VerificationResponse submitIdVerification(Long userId, MultipartFile document,
                                                      MultipartFile selfie, String documentType) {
        Profile profile = getProfile(userId);

        if (verificationRepo.existsByProfileIdAndStatus(profile.getId(), VerificationStatus.PENDING)) {
            throw new BadRequestException("You already have a pending verification request");
        }

        DocumentType docType;
        try {
            docType = DocumentType.valueOf(documentType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid document type. Valid: AADHAAR, PAN, VOTER_ID, PASSPORT, DRIVING_LICENSE");
        }

        try {
            Map<String, String> docKeys = storageService.uploadProfilePhoto(userId, document);
            String selfieKey = selfie != null ? storageService.uploadProfilePhoto(userId, selfie).get("original") : null;

            VerificationRequest request = VerificationRequest.builder()
                    .profile(profile)
                    .documentType(docType)
                    .documentKey(docKeys.get("original"))
                    .selfieKey(selfieKey)
                    .build();

            request = verificationRepo.save(request);
            log.info("ID verification submitted: profileId={}, type={}", profile.getId(), docType);

            return toVerificationResponse(request);
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload document: " + e.getMessage());
        }
    }

    @Transactional
    public VerificationResponse reviewVerification(Long adminUserId, Long requestId, ReviewVerificationRequest review) {
        VerificationRequest request = verificationRepo.findById(requestId)
                .orElseThrow(() -> new ResourceNotFoundException("Verification request", "id", requestId));

        User admin = userRepository.findById(adminUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", adminUserId));

        VerificationStatus status;
        try {
            status = VerificationStatus.valueOf(review.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid status. Use APPROVED or REJECTED");
        }

        request.setStatus(status);
        request.setReviewedBy(admin);
        request.setReviewedAt(LocalDateTime.now());
        if (status == VerificationStatus.REJECTED) {
            request.setRejectionReason(review.getRejectionReason());
        }

        request = verificationRepo.save(request);

        // Update trust score
        if (status == VerificationStatus.APPROVED) {
            updateTrustScore(request.getProfile().getId());
        }

        return toVerificationResponse(request);
    }

    // =============================================
    //  Photo Verification
    // =============================================
    @Transactional
    public VerificationResponse submitPhotoVerification(Long userId, MultipartFile selfie) {
        Profile profile = getProfile(userId);

        try {
            Map<String, String> keys = storageService.uploadProfilePhoto(userId, selfie);

            PhotoVerification pv = PhotoVerification.builder()
                    .profile(profile)
                    .selfieKey(keys.get("original"))
                    .build();

            pv = photoVerificationRepo.save(pv);
            log.info("Photo verification submitted: profileId={}", profile.getId());

            return VerificationResponse.builder()
                    .id(pv.getId())
                    .documentType("SELFIE")
                    .selfieUrl(publicUrl + "/" + keys.get("original"))
                    .status(pv.getStatus().name())
                    .createdAt(pv.getCreatedAt())
                    .build();
        } catch (IOException e) {
            throw new BadRequestException("Failed to upload selfie: " + e.getMessage());
        }
    }

    // =============================================
    //  Community Endorsements
    // =============================================
    @Transactional
    public EndorsementResponse endorseProfile(Long userId, Long targetProfileId, EndorsementRequest request) {
        Profile endorser = getProfile(userId);
        Profile target = profileRepository.findById(targetProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", targetProfileId));

        if (endorser.getId().equals(target.getId())) {
            throw new BadRequestException("Cannot endorse your own profile");
        }

        if (endorsementRepo.existsByProfileIdAndEndorserProfileId(target.getId(), endorser.getId())) {
            throw new BadRequestException("You have already endorsed this profile");
        }

        if (endorsementRepo.countByProfileIdAndApprovedTrue(target.getId()) >= 3) {
            throw new BadRequestException("This profile already has the maximum 3 endorsements");
        }

        CommunityEndorsement endorsement = CommunityEndorsement.builder()
                .profile(target)
                .endorserProfile(endorser)
                .endorsementText(request.getEndorsementText())
                .relationship(request.getRelationship())
                .approved(true) // Auto-approve for now; can add moderation later
                .approvedAt(LocalDateTime.now())
                .build();

        endorsement = endorsementRepo.save(endorsement);

        // Update trust score
        updateTrustScore(target.getId());

        return toEndorsementResponse(endorsement);
    }

    @Transactional(readOnly = true)
    public List<EndorsementResponse> getEndorsements(Long profileId) {
        return endorsementRepo.findByProfileIdAndApprovedTrue(profileId).stream()
                .map(this::toEndorsementResponse)
                .toList();
    }

    // =============================================
    //  Samaj Sabha Directory
    // =============================================
    @Transactional
    public SabhaResponse createSabha(CreateSabhaRequest request) {
        SamajSabha sabha = SamajSabha.builder()
                .name(request.getName())
                .city(request.getCity())
                .state(request.getState())
                .address(request.getAddress())
                .contactPerson(request.getContactPerson())
                .contactPhone(request.getContactPhone())
                .contactEmail(request.getContactEmail())
                .build();
        sabha = sabhaRepo.save(sabha);
        return toSabhaResponse(sabha, null);
    }

    @Transactional(readOnly = true)
    public List<SabhaResponse> getSabhasByCity(String city, Long userId) {
        Profile profile = userId != null ? profileRepository.findByUserId(userId).orElse(null) : null;
        return sabhaRepo.findByCityIgnoreCaseAndActiveTrue(city).stream()
                .map(s -> toSabhaResponse(s, profile))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SabhaResponse> getSabhasByState(String state, Long userId) {
        Profile profile = userId != null ? profileRepository.findByUserId(userId).orElse(null) : null;
        return sabhaRepo.findByStateIgnoreCaseAndActiveTrue(state).stream()
                .map(s -> toSabhaResponse(s, profile))
                .toList();
    }

    @Transactional
    public void joinSabha(Long userId, Long sabhaId) {
        Profile profile = getProfile(userId);
        SamajSabha sabha = sabhaRepo.findById(sabhaId)
                .orElseThrow(() -> new ResourceNotFoundException("Sabha", "id", sabhaId));

        if (sabhaLinkRepo.existsByProfileIdAndSabhaId(profile.getId(), sabhaId)) {
            throw new BadRequestException("Already a member of this Sabha");
        }

        sabhaLinkRepo.save(ProfileSabhaLink.builder()
                .profile(profile)
                .sabha(sabha)
                .build());

        sabha.setMemberCount(sabha.getMemberCount() + 1);
        sabhaRepo.save(sabha);
    }

    // =============================================
    //  Trust Score
    // =============================================
    @Transactional
    public TrustScoreResponse getTrustScore(Long profileId) {
        TrustScore score = trustScoreRepo.findByProfileId(profileId)
                .orElseGet(() -> {
                    Profile profile = profileRepository.findById(profileId)
                            .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", profileId));
                    TrustScore newScore = TrustScore.builder().profile(profile).build();
                    return trustScoreRepo.save(newScore);
                });

        // Always recalculate
        updateTrustScoreInternal(score, profileId);

        List<String> badges = new ArrayList<>();
        if (score.getIdVerificationScore() > 0) badges.add("ID_VERIFIED");
        if (score.getPhotoVerificationScore() > 0) badges.add("PHOTO_VERIFIED");
        if (score.getSabhaVerificationScore() > 0) badges.add("SABHA_VERIFIED");
        if (score.getEndorsementScore() > 0) badges.add("COMMUNITY_ENDORSED");
        if (score.getTotalScore() >= 70) badges.add("HIGHLY_TRUSTED");

        return TrustScoreResponse.builder()
                .totalScore(score.getTotalScore())
                .idVerificationScore(score.getIdVerificationScore())
                .photoVerificationScore(score.getPhotoVerificationScore())
                .endorsementScore(score.getEndorsementScore())
                .sabhaVerificationScore(score.getSabhaVerificationScore())
                .completenessScore(score.getCompletenessScore())
                .responseRateScore(score.getResponseRateScore())
                .badges(badges)
                .updatedAt(score.getUpdatedAt())
                .build();
    }

    @Transactional
    public void updateTrustScore(Long profileId) {
        TrustScore score = trustScoreRepo.findByProfileId(profileId)
                .orElseGet(() -> {
                    Profile profile = profileRepository.findById(profileId)
                            .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", profileId));
                    return trustScoreRepo.save(TrustScore.builder().profile(profile).build());
                });
        updateTrustScoreInternal(score, profileId);
    }

    private void updateTrustScoreInternal(TrustScore score, Long profileId) {
        // ID Verification (+20)
        score.setIdVerificationScore(
                verificationRepo.existsByProfileIdAndStatus(profileId, VerificationStatus.APPROVED) ? 20 : 0);

        // Photo Verification (+10)
        score.setPhotoVerificationScore(
                photoVerificationRepo.existsByProfileIdAndStatus(profileId, VerificationStatus.APPROVED) ? 10 : 0);

        // Endorsements (+5 each, max 3 = 15)
        int endorsementCount = endorsementRepo.countByProfileIdAndApprovedTrue(profileId);
        score.setEndorsementScore(Math.min(15, endorsementCount * 5));

        // Sabha verification (+30)
        List<ProfileSabhaLink> sabhaLinks = sabhaLinkRepo.findByProfileId(profileId);
        boolean sabhaVerified = sabhaLinks.stream().anyMatch(ProfileSabhaLink::isVerifiedByLeader);
        score.setSabhaVerificationScore(sabhaVerified ? 30 : 0);

        // Profile completeness (+10)
        Profile profile = profileRepository.findById(profileId).orElse(null);
        if (profile != null && profile.getCompletenessScore() >= 80) {
            score.setCompletenessScore(10);
        } else if (profile != null && profile.getCompletenessScore() >= 50) {
            score.setCompletenessScore(5);
        } else {
            score.setCompletenessScore(0);
        }

        // Response rate (+15) — based on interest response ratio
        // Simplified: if profile has responded to >50% of received interests
        score.setResponseRateScore(10); // Default mid-score for now

        score.recalculate();
        trustScoreRepo.save(score);
    }

    // =============================================
    //  Mutual Connections
    // =============================================
    @Transactional(readOnly = true)
    public MutualConnectionResponse getMutualConnections(Long userId, Long targetProfileId) {
        Profile myProfile = getProfile(userId);
        Profile targetProfile = profileRepository.findById(targetProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", targetProfileId));

        // Mutual Sabhas
        List<ProfileSabhaLink> myLinks = sabhaLinkRepo.findByProfileId(myProfile.getId());
        List<ProfileSabhaLink> theirLinks = sabhaLinkRepo.findByProfileId(targetProfile.getId());

        Set<Long> mySabhaIds = myLinks.stream().map(l -> l.getSabha().getId()).collect(Collectors.toSet());
        List<String> mutualSabhas = theirLinks.stream()
                .filter(l -> mySabhaIds.contains(l.getSabha().getId()))
                .map(l -> l.getSabha().getName())
                .toList();

        // Mutual endorsers
        List<CommunityEndorsement> myEndorsements = endorsementRepo.findByProfileIdAndApprovedTrue(myProfile.getId());
        List<CommunityEndorsement> theirEndorsements = endorsementRepo.findByProfileIdAndApprovedTrue(targetProfile.getId());

        Set<Long> myEndorserIds = myEndorsements.stream().map(e -> e.getEndorserProfile().getId()).collect(Collectors.toSet());
        int mutualEndorsers = (int) theirEndorsements.stream()
                .filter(e -> myEndorserIds.contains(e.getEndorserProfile().getId()))
                .count();

        return MutualConnectionResponse.builder()
                .mutualSabhaCount(mutualSabhas.size())
                .mutualSabhaNames(mutualSabhas)
                .mutualEndorsers(mutualEndorsers)
                .build();
    }

    // =============================================
    //  Admin: Verification Queue
    // =============================================
    @Transactional(readOnly = true)
    public Page<VerificationResponse> getPendingVerifications(int page, int size) {
        return verificationRepo.findByStatus(VerificationStatus.PENDING, PageRequest.of(page, size))
                .map(this::toVerificationResponse);
    }

    // =============================================
    //  Helpers
    // =============================================
    private Profile getProfile(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));
    }

    private VerificationResponse toVerificationResponse(VerificationRequest r) {
        return VerificationResponse.builder()
                .id(r.getId())
                .documentType(r.getDocumentType().name())
                .documentUrl(publicUrl + "/" + r.getDocumentKey())
                .selfieUrl(r.getSelfieKey() != null ? publicUrl + "/" + r.getSelfieKey() : null)
                .status(r.getStatus().name())
                .rejectionReason(r.getRejectionReason())
                .createdAt(r.getCreatedAt())
                .reviewedAt(r.getReviewedAt())
                .build();
    }

    private EndorsementResponse toEndorsementResponse(CommunityEndorsement e) {
        return EndorsementResponse.builder()
                .id(e.getId())
                .endorserProfileId(e.getEndorserProfile().getId())
                .endorserName(e.getEndorserProfile().getFirstName() + " " + e.getEndorserProfile().getLastName())
                .endorserGotra(e.getEndorserProfile().getGotra().getDisplayName())
                .endorsementText(e.getEndorsementText())
                .relationship(e.getRelationship())
                .approved(e.isApproved())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private SabhaResponse toSabhaResponse(SamajSabha s, Profile profile) {
        boolean isMember = false;
        boolean verified = false;
        if (profile != null) {
            var link = sabhaLinkRepo.findByProfileId(profile.getId()).stream()
                    .filter(l -> l.getSabha().getId().equals(s.getId()))
                    .findFirst();
            isMember = link.isPresent();
            verified = link.map(ProfileSabhaLink::isVerifiedByLeader).orElse(false);
        }

        return SabhaResponse.builder()
                .id(s.getId()).name(s.getName()).city(s.getCity()).state(s.getState())
                .address(s.getAddress()).contactPerson(s.getContactPerson())
                .contactPhone(s.getContactPhone()).memberCount(s.getMemberCount())
                .myMembership(isMember).verifiedByLeader(verified)
                .build();
    }
}
