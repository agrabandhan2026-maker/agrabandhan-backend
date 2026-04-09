package com.agrabandhan.profile.service;

import com.agrabandhan.common.exception.BadRequestException;
import com.agrabandhan.common.exception.ResourceNotFoundException;
import com.agrabandhan.common.util.StorageService;
import com.agrabandhan.profile.dto.ProfileDto.PhotoResponse;
import com.agrabandhan.profile.entity.Profile;
import com.agrabandhan.profile.entity.ProfilePhoto;
import com.agrabandhan.profile.repository.ProfilePhotoRepository;
import com.agrabandhan.profile.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class PhotoService {

    private final ProfileRepository profileRepository;
    private final ProfilePhotoRepository photoRepository;
    private final StorageService storageService;

    private static final int MAX_PHOTOS = 8;

    @Transactional
    public PhotoResponse uploadPhoto(Long userId, MultipartFile file, String visibility) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        int currentCount = photoRepository.countByProfileId(profile.getId());
        if (currentCount >= MAX_PHOTOS) {
            throw new BadRequestException("Maximum " + MAX_PHOTOS + " photos allowed. Delete an existing photo first.");
        }

        try {
            Map<String, String> keys = storageService.uploadProfilePhoto(userId, file);

            ProfilePhoto.PhotoVisibility photoVisibility = ProfilePhoto.PhotoVisibility.PUBLIC;
            if (visibility != null) {
                try {
                    photoVisibility = ProfilePhoto.PhotoVisibility.valueOf(visibility.toUpperCase());
                } catch (IllegalArgumentException ignored) {
                }
            }

            ProfilePhoto photo = ProfilePhoto.builder()
                    .profile(profile)
                    .originalKey(keys.get("original"))
                    .mediumKey(keys.get("medium"))
                    .thumbnailKey(keys.get("thumbnail"))
                    .displayOrder(currentCount)
                    .primary(currentCount == 0) // First photo is automatically primary
                    .visibility(photoVisibility)
                    .build();

            photo = photoRepository.save(photo);
            log.info("Photo uploaded for userId: {}, photoId: {}", userId, photo.getId());

            return PhotoResponse.from(photo, storageService.getPublicBaseUrl());

        } catch (IOException e) {
            log.error("Failed to upload photo for userId: {}", userId, e);
            throw new BadRequestException("Failed to upload photo: " + e.getMessage());
        }
    }

    @Transactional
    public void deletePhoto(Long userId, Long photoId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        ProfilePhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo", "id", photoId));

        if (!photo.getProfile().getId().equals(profile.getId())) {
            throw new BadRequestException("This photo does not belong to your profile");
        }

        // Delete from R2
        storageService.deleteFile(photo.getOriginalKey());
        if (photo.getMediumKey() != null) storageService.deleteFile(photo.getMediumKey());
        if (photo.getThumbnailKey() != null) storageService.deleteFile(photo.getThumbnailKey());

        boolean wasPrimary = photo.isPrimary();
        photoRepository.delete(photo);

        // If deleted photo was primary, make the first remaining photo primary
        if (wasPrimary) {
            List<ProfilePhoto> remaining = photoRepository.findByProfileIdOrderByDisplayOrderAsc(profile.getId());
            if (!remaining.isEmpty()) {
                remaining.get(0).setPrimary(true);
                photoRepository.save(remaining.get(0));
            }
        }

        log.info("Photo deleted: photoId={}, userId={}", photoId, userId);
    }

    @Transactional
    public PhotoResponse setPrimaryPhoto(Long userId, Long photoId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        ProfilePhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo", "id", photoId));

        if (!photo.getProfile().getId().equals(profile.getId())) {
            throw new BadRequestException("This photo does not belong to your profile");
        }

        // Clear existing primary and set new one
        photoRepository.clearPrimaryFlag(profile.getId());
        photo.setPrimary(true);
        photo = photoRepository.save(photo);

        return PhotoResponse.from(photo, storageService.getPublicBaseUrl());
    }

    @Transactional(readOnly = true)
    public List<PhotoResponse> getPhotos(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        return photoRepository.findByProfileIdOrderByDisplayOrderAsc(profile.getId()).stream()
                .map(p -> PhotoResponse.from(p, storageService.getPublicBaseUrl()))
                .toList();
    }

    @Transactional
    public PhotoResponse updateVisibility(Long userId, Long photoId, String visibility) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Profile", "userId", userId));

        ProfilePhoto photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new ResourceNotFoundException("Photo", "id", photoId));

        if (!photo.getProfile().getId().equals(profile.getId())) {
            throw new BadRequestException("This photo does not belong to your profile");
        }

        try {
            photo.setVisibility(ProfilePhoto.PhotoVisibility.valueOf(visibility.toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid visibility. Allowed: PUBLIC, CONNECTIONS_ONLY, HIDDEN");
        }

        photo = photoRepository.save(photo);
        return PhotoResponse.from(photo, storageService.getPublicBaseUrl());
    }
}
