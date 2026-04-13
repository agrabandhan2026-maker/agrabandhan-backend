package com.agrabandhan.profile.family;

import com.agrabandhan.auth.entity.User;
import com.agrabandhan.auth.repository.UserRepository;
import com.agrabandhan.common.dto.PagedResponse;
import com.agrabandhan.common.exception.BadRequestException;
import com.agrabandhan.common.exception.ResourceNotFoundException;
import com.agrabandhan.common.util.StorageService;
import com.agrabandhan.profile.entity.Profile;
import com.agrabandhan.profile.family.FamilyDto.*;
import com.agrabandhan.profile.family.FamilyMember.InviteStatus;
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
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FamilyDashboardService {

    private final FamilyMemberRepository familyMemberRepository;
    private final ActivityLogRepository activityLogRepository;
    private final VoiceIntroductionRepository voiceIntroRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Value("${agrabandhan.r2.public-url:}")
    private String publicUrl;

    private static final int MAX_FAMILY_MEMBERS = 4;

    // =============================================
    //  Family Dashboard Overview
    // =============================================
    @Transactional(readOnly = true)
    public FamilyDashboardResponse getDashboard(Long userId) {
        Profile profile = getProfile(userId);

        List<FamilyMember> members = familyMemberRepository.findByProfileIdAndActiveTrue(profile.getId());
        VoiceIntroduction voiceIntro = voiceIntroRepository.findByProfileIdAndActiveTrue(profile.getId()).orElse(null);

        return FamilyDashboardResponse.builder()
                .profileId(profile.getId())
                .profileName(profile.getFirstName() + " " + profile.getLastName())
                .members(members.stream().map(this::toMemberResponse).toList())
                .voiceIntro(voiceIntro != null ? toVoiceResponse(voiceIntro) : null)
                .memberCount(members.size())
                .maxMembers(MAX_FAMILY_MEMBERS)
                .build();
    }

    // =============================================
    //  Add Family Member
    // =============================================
    @Transactional
    public FamilyMemberResponse addFamilyMember(Long userId, AddFamilyMemberRequest request) {
        Profile profile = getProfile(userId);

        int currentCount = familyMemberRepository.countByProfileIdAndActiveTrue(profile.getId());
        if (currentCount >= MAX_FAMILY_MEMBERS) {
            throw new BadRequestException("Maximum " + MAX_FAMILY_MEMBERS + " family members allowed");
        }

        String inviteToken = UUID.randomUUID().toString().replace("-", "");

        FamilyMember member = FamilyMember.builder()
                .profile(profile)
                .name(request.getName())
                .relationship(request.getRelationship())
                .phoneNumber(request.getPhoneNumber())
                .canSearch(request.getCanSearch() != null ? request.getCanSearch() : true)
                .canShortlist(request.getCanShortlist() != null ? request.getCanShortlist() : true)
                .canSendInterest(request.getCanSendInterest() != null ? request.getCanSendInterest() : false)
                .canAcceptInterest(request.getCanAcceptInterest() != null ? request.getCanAcceptInterest() : false)
                .canChat(request.getCanChat() != null ? request.getCanChat() : false)
                .canEditProfile(request.getCanEditProfile() != null ? request.getCanEditProfile() : false)
                .inviteToken(inviteToken)
                .inviteExpiresAt(LocalDateTime.now().plusDays(7))
                .build();

        member = familyMemberRepository.save(member);

        // Log activity
        logActivity(profile, userId, ActivityLog.ActionType.FAMILY_MEMBER_ADDED,
                "Added " + request.getRelationship() + ": " + request.getName());

        log.info("Family member added: profileId={}, name={}, relationship={}",
                profile.getId(), request.getName(), request.getRelationship());

        return toMemberResponse(member);
    }

    // =============================================
    //  Accept Invite (family member joins)
    // =============================================
    @Transactional
    public FamilyMemberResponse acceptInvite(Long userId, String inviteToken) {
        FamilyMember member = familyMemberRepository.findByInviteToken(inviteToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired invite token"));

        if (member.getInviteStatus() == InviteStatus.ACCEPTED) {
            throw new BadRequestException("Invite already accepted");
        }

        if (member.getInviteExpiresAt() != null && member.getInviteExpiresAt().isBefore(LocalDateTime.now())) {
            member.setInviteStatus(InviteStatus.EXPIRED);
            familyMemberRepository.save(member);
            throw new BadRequestException("Invite has expired. Request a new one from the profile owner.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        member.setUser(user);
        member.setInviteStatus(InviteStatus.ACCEPTED);
        member = familyMemberRepository.save(member);

        log.info("Family invite accepted: memberId={}, userId={}", member.getId(), userId);

        return toMemberResponse(member);
    }

    // =============================================
    //  Update Permissions
    // =============================================
    @Transactional
    public FamilyMemberResponse updatePermissions(Long userId, Long memberId, UpdatePermissionsRequest request) {
        Profile profile = getProfile(userId);
        FamilyMember member = familyMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Family member", "id", memberId));

        if (!member.getProfile().getId().equals(profile.getId())) {
            throw new BadRequestException("This family member doesn't belong to your profile");
        }

        if (request.getCanSearch() != null) member.setCanSearch(request.getCanSearch());
        if (request.getCanShortlist() != null) member.setCanShortlist(request.getCanShortlist());
        if (request.getCanSendInterest() != null) member.setCanSendInterest(request.getCanSendInterest());
        if (request.getCanAcceptInterest() != null) member.setCanAcceptInterest(request.getCanAcceptInterest());
        if (request.getCanChat() != null) member.setCanChat(request.getCanChat());
        if (request.getCanEditProfile() != null) member.setCanEditProfile(request.getCanEditProfile());

        member = familyMemberRepository.save(member);
        return toMemberResponse(member);
    }

    // =============================================
    //  Remove Family Member
    // =============================================
    @Transactional
    public void removeFamilyMember(Long userId, Long memberId) {
        Profile profile = getProfile(userId);
        FamilyMember member = familyMemberRepository.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Family member", "id", memberId));

        if (!member.getProfile().getId().equals(profile.getId())) {
            throw new BadRequestException("This family member doesn't belong to your profile");
        }

        member.setActive(false);
        familyMemberRepository.save(member);

        logActivity(profile, userId, ActivityLog.ActionType.FAMILY_MEMBER_REMOVED,
                "Removed " + member.getRelationship() + ": " + member.getName());
    }

    // =============================================
    //  Activity Log
    // =============================================
    @Transactional(readOnly = true)
    public PagedResponse<ActivityLogResponse> getActivityLog(Long userId, int page, int size) {
        Profile profile = getProfile(userId);

        Page<ActivityLog> logs = activityLogRepository.findByProfileIdOrderByCreatedAtDesc(
                profile.getId(), PageRequest.of(page, Math.min(size, 50)));

        Page<ActivityLogResponse> responsePage = logs.map(l -> ActivityLogResponse.builder()
                .id(l.getId())
                .actorName(l.getActorName())
                .actorRelationship(l.getActorRelationship())
                .actionType(l.getActionType().name())
                .actionDetail(l.getActionDetail())
                .createdAt(l.getCreatedAt())
                .build());

        return PagedResponse.from(responsePage);
    }

    // =============================================
    //  Voice Introduction
    // =============================================
    @Transactional
    public VoiceIntroResponse uploadVoiceIntro(Long userId, MultipartFile file, String recordedByName,
                                                String relationship) {
        Profile profile = getProfile(userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        // Deactivate existing voice intro
        voiceIntroRepository.findByProfileIdAndActiveTrue(profile.getId())
                .ifPresent(existing -> {
                    existing.setActive(false);
                    voiceIntroRepository.save(existing);
                    storageService.deleteFile(existing.getAudioKey());
                });

        try {
            String audioKey = storageService.uploadVoiceIntro(userId, file);

            // Estimate duration (rough: file size / bitrate)
            int estimatedDuration = (int) (file.getSize() / 16000); // ~128kbps
            estimatedDuration = Math.min(estimatedDuration, 60); // Cap at 60 seconds

            VoiceIntroduction voiceIntro = VoiceIntroduction.builder()
                    .profile(profile)
                    .recordedByUser(user)
                    .recordedByName(recordedByName != null ? recordedByName : profile.getFirstName())
                    .recordedByRelationship(relationship)
                    .audioKey(audioKey)
                    .durationSeconds(estimatedDuration)
                    .fileSizeBytes(file.getSize())
                    .build();

            voiceIntro = voiceIntroRepository.save(voiceIntro);

            logActivity(profile, userId, ActivityLog.ActionType.VOICE_INTRO_UPLOADED,
                    "Voice introduction uploaded by " + (recordedByName != null ? recordedByName : "self"));

            log.info("Voice intro uploaded: profileId={}", profile.getId());

            return toVoiceResponse(voiceIntro);

        } catch (IOException e) {
            throw new BadRequestException("Failed to upload voice introduction: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public VoiceIntroResponse getVoiceIntro(Long profileId) {
        VoiceIntroduction voiceIntro = voiceIntroRepository.findByProfileIdAndActiveTrue(profileId)
                .orElseThrow(() -> new ResourceNotFoundException("No voice introduction found for this profile"));
        return toVoiceResponse(voiceIntro);
    }

    @Transactional
    public void deleteVoiceIntro(Long userId) {
        Profile profile = getProfile(userId);
        voiceIntroRepository.findByProfileIdAndActiveTrue(profile.getId())
                .ifPresent(v -> {
                    v.setActive(false);
                    voiceIntroRepository.save(v);
                    storageService.deleteFile(v.getAudioKey());
                });
    }

    // =============================================
    //  Activity Logger (public for other services)
    // =============================================
    @Transactional
    public void logActivity(Profile profile, Long actorUserId, ActivityLog.ActionType actionType, String detail) {
        User actor = userRepository.findById(actorUserId).orElse(null);
        if (actor == null) return;

        // Check if actor is a family member
        String actorName = profile.getFirstName();
        String actorRelationship = "SELF";

        FamilyMember member = familyMemberRepository.findByProfileIdAndUserId(profile.getId(), actorUserId).orElse(null);
        if (member != null) {
            actorName = member.getName();
            actorRelationship = member.getRelationship().name();
        }

        activityLogRepository.save(ActivityLog.builder()
                .profile(profile)
                .actorUser(actor)
                .actorName(actorName)
                .actorRelationship(actorRelationship)
                .actionType(actionType)
                .actionDetail(detail)
                .build());
    }

    // =============================================
    //  Helpers
    // =============================================
    private Profile getProfile(Long userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));
    }

    private FamilyMemberResponse toMemberResponse(FamilyMember m) {
        return FamilyMemberResponse.builder()
                .id(m.getId())
                .name(m.getName())
                .relationship(m.getRelationship().name())
                .phoneNumber(m.getPhoneNumber())
                .canSearch(m.isCanSearch())
                .canShortlist(m.isCanShortlist())
                .canSendInterest(m.isCanSendInterest())
                .canAcceptInterest(m.isCanAcceptInterest())
                .canChat(m.isCanChat())
                .canEditProfile(m.isCanEditProfile())
                .inviteStatus(m.getInviteStatus().name())
                .inviteToken(m.getInviteToken())
                .active(m.isActive())
                .createdAt(m.getCreatedAt())
                .build();
    }

    private VoiceIntroResponse toVoiceResponse(VoiceIntroduction v) {
        return VoiceIntroResponse.builder()
                .id(v.getId())
                .audioUrl(publicUrl + "/" + v.getAudioKey())
                .recordedByName(v.getRecordedByName())
                .recordedByRelationship(v.getRecordedByRelationship())
                .durationSeconds(v.getDurationSeconds())
                .active(v.isActive())
                .createdAt(v.getCreatedAt())
                .build();
    }
}
