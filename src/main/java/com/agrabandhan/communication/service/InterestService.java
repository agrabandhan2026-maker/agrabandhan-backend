package com.agrabandhan.communication.service;

import com.agrabandhan.common.dto.PagedResponse;
import com.agrabandhan.common.exception.BadRequestException;
import com.agrabandhan.common.exception.ResourceNotFoundException;
import com.agrabandhan.communication.entity.Interest;
import com.agrabandhan.communication.entity.Interest.InterestStatus;
import com.agrabandhan.communication.repository.InterestRepository;
import com.agrabandhan.matching.dto.MatchingDto.*;
import com.agrabandhan.profile.entity.Profile;
import com.agrabandhan.profile.entity.ProfilePhoto;
import com.agrabandhan.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterestService {

    private final InterestRepository interestRepository;
    private final ProfileRepository profileRepository;
    private final ChatService chatService;

    @Value("${agrabandhan.r2.public-url:}")
    private String photoBaseUrl;

    @Transactional
    public InterestResponse sendInterest(Long userId, Long targetProfileId, SendInterestRequest request) {
        Profile sender = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));
        Profile receiver = profileRepository.findById(targetProfileId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "id", targetProfileId));

        // Validations
        if (sender.getId().equals(receiver.getId())) {
            throw new BadRequestException("Cannot send interest to yourself");
        }

        if (sender.getGotra() == receiver.getGotra()) {
            throw new BadRequestException("Cannot send interest to same gotra profile");
        }

        if (sender.getGender() == receiver.getGender()) {
            throw new BadRequestException("Cannot send interest to same gender profile");
        }

        // Check existing interest
        var existing = interestRepository.findBySenderProfileIdAndReceiverProfileId(
                sender.getId(), receiver.getId());
        if (existing.isPresent()) {
            Interest ex = existing.get();
            if (ex.getStatus() == InterestStatus.PENDING) {
                throw new BadRequestException("Interest already sent and pending");
            }
            if (ex.getStatus() == InterestStatus.ACCEPTED) {
                throw new BadRequestException("Interest already accepted");
            }
            if (ex.getStatus() == InterestStatus.DECLINED || ex.getStatus() == InterestStatus.WITHDRAWN) {
                // Allow re-sending after decline/withdraw
                ex.setStatus(InterestStatus.PENDING);
                ex.setMessage(request != null ? request.getMessage() : null);
                ex.setDeclineReason(null);
                ex.setSentAt(LocalDateTime.now());
                ex.setRespondedAt(null);
                ex = interestRepository.save(ex);
                return toResponse(ex, receiver, "SENT");
            }
        }

        Interest interest = Interest.builder()
                .senderProfile(sender)
                .receiverProfile(receiver)
                .message(request != null ? request.getMessage() : null)
                .build();

        interest = interestRepository.save(interest);
        log.info("Interest sent: {} -> {}", sender.getId(), receiver.getId());

        // TODO: Send push notification to receiver

        return toResponse(interest, receiver, "SENT");
    }

    @Transactional
    public InterestResponse acceptInterest(Long userId, Long interestId) {
        Profile myProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResourceNotFoundException("Interest", "id", interestId));

        if (!interest.getReceiverProfile().getId().equals(myProfile.getId())) {
            throw new BadRequestException("You can only accept interests sent to you");
        }

        if (interest.getStatus() != InterestStatus.PENDING) {
            throw new BadRequestException("Interest is not in pending state");
        }

        interest.setStatus(InterestStatus.ACCEPTED);
        interest.setRespondedAt(LocalDateTime.now());
        interest = interestRepository.save(interest);

        // Create conversation for chat (auto-created on acceptance)
        chatService.getOrCreateConversation(myProfile.getId(), interest.getSenderProfile().getId());

        log.info("Interest accepted: {} accepted by {}", interestId, myProfile.getId());

        // TODO: Send push notification to sender

        return toResponse(interest, interest.getSenderProfile(), "RECEIVED");
    }

    @Transactional
    public InterestResponse declineInterest(Long userId, Long interestId, DeclineInterestRequest request) {
        Profile myProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResourceNotFoundException("Interest", "id", interestId));

        if (!interest.getReceiverProfile().getId().equals(myProfile.getId())) {
            throw new BadRequestException("You can only decline interests sent to you");
        }

        if (interest.getStatus() != InterestStatus.PENDING) {
            throw new BadRequestException("Interest is not in pending state");
        }

        interest.setStatus(InterestStatus.DECLINED);
        interest.setDeclineReason(request != null ? request.getReason() : null);
        interest.setRespondedAt(LocalDateTime.now());
        interest = interestRepository.save(interest);

        log.info("Interest declined: {} declined by {}", interestId, myProfile.getId());

        return toResponse(interest, interest.getSenderProfile(), "RECEIVED");
    }

    @Transactional
    public InterestResponse withdrawInterest(Long userId, Long interestId) {
        Profile myProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        Interest interest = interestRepository.findById(interestId)
                .orElseThrow(() -> new ResourceNotFoundException("Interest", "id", interestId));

        if (!interest.getSenderProfile().getId().equals(myProfile.getId())) {
            throw new BadRequestException("You can only withdraw interests you sent");
        }

        if (interest.getStatus() != InterestStatus.PENDING) {
            throw new BadRequestException("Can only withdraw pending interests");
        }

        interest.setStatus(InterestStatus.WITHDRAWN);
        interest.setRespondedAt(LocalDateTime.now());
        interest = interestRepository.save(interest);

        log.info("Interest withdrawn: {} by {}", interestId, myProfile.getId());

        return toResponse(interest, interest.getReceiverProfile(), "SENT");
    }

    // =============================================
    //  Get Interests
    // =============================================
    @Transactional(readOnly = true)
    public PagedResponse<InterestResponse> getSentInterests(Long userId, String status, int page, int size) {
        Profile myProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        Page<Interest> interests;
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, 50));

        if (status != null && !status.isBlank()) {
            interests = interestRepository.findSentInterests(myProfile.getId(),
                    InterestStatus.valueOf(status.toUpperCase()), pageRequest);
        } else {
            interests = interestRepository.findAllSent(myProfile.getId(), pageRequest);
        }

        return PagedResponse.from(interests.map(i -> toResponse(i, i.getReceiverProfile(), "SENT")));
    }

    @Transactional(readOnly = true)
    public PagedResponse<InterestResponse> getReceivedInterests(Long userId, String status, int page, int size) {
        Profile myProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        Page<Interest> interests;
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, 50));

        if (status != null && !status.isBlank()) {
            interests = interestRepository.findReceivedInterests(myProfile.getId(),
                    InterestStatus.valueOf(status.toUpperCase()), pageRequest);
        } else {
            interests = interestRepository.findAllReceived(myProfile.getId(), pageRequest);
        }

        return PagedResponse.from(interests.map(i -> toResponse(i, i.getSenderProfile(), "RECEIVED")));
    }

    @Transactional(readOnly = true)
    public InterestCountResponse getInterestCounts(Long userId) {
        Profile myProfile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        long pendingReceived = interestRepository.countByReceiverProfileIdAndStatus(
                myProfile.getId(), InterestStatus.PENDING);

        return InterestCountResponse.builder()
                .pendingReceived(pendingReceived)
                .build();
    }

    // =============================================
    //  Helper
    // =============================================
    private InterestResponse toResponse(Interest interest, Profile otherProfile, String direction) {
        Integer age = otherProfile.getDateOfBirth() != null
                ? Period.between(otherProfile.getDateOfBirth(), LocalDate.now()).getYears() : null;

        String photo = otherProfile.getPhotos() != null ? otherProfile.getPhotos().stream()
                .filter(ProfilePhoto::isPrimary).findFirst()
                .map(p -> photoBaseUrl + "/" + p.getThumbnailKey())
                .orElse(null) : null;

        return InterestResponse.builder()
                .interestId(interest.getId())
                .profileId(otherProfile.getId())
                .firstName(otherProfile.getFirstName())
                .lastName(otherProfile.getLastName())
                .age(age)
                .gotra(otherProfile.getGotra().getDisplayName())
                .currentCity(otherProfile.getCurrentCity())
                .primaryPhotoUrl(photo)
                .status(interest.getStatus().name())
                .message(interest.getMessage())
                .declineReason(interest.getDeclineReason())
                .sentAt(interest.getSentAt() != null ? interest.getSentAt().toString() : null)
                .respondedAt(interest.getRespondedAt() != null ? interest.getRespondedAt().toString() : null)
                .direction(direction)
                .build();
    }
}
