package com.agrabandhan.profile.controller;

import com.agrabandhan.auth.entity.User;
import com.agrabandhan.common.dto.ApiResponse;
import com.agrabandhan.profile.dto.ProfileDto.PhotoResponse;
import com.agrabandhan.profile.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/profiles/photos")
@RequiredArgsConstructor
@Tag(name = "Photos", description = "Profile photo upload and management")
public class PhotoController {

    private final PhotoService photoService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a profile photo (max 8, max 5MB each)")
    public ResponseEntity<ApiResponse<PhotoResponse>> uploadPhoto(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "visibility", required = false, defaultValue = "PUBLIC") String visibility) {
        PhotoResponse response = photoService.uploadPhoto(user.getId(), file, visibility);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Photo uploaded successfully", response));
    }

    @GetMapping
    @Operation(summary = "Get all my photos")
    public ResponseEntity<ApiResponse<List<PhotoResponse>>> getMyPhotos(
            @AuthenticationPrincipal User user) {
        List<PhotoResponse> photos = photoService.getPhotos(user.getId());
        return ResponseEntity.ok(ApiResponse.success(photos));
    }

    @DeleteMapping("/{photoId}")
    @Operation(summary = "Delete a photo")
    public ResponseEntity<ApiResponse<Void>> deletePhoto(
            @AuthenticationPrincipal User user,
            @PathVariable Long photoId) {
        photoService.deletePhoto(user.getId(), photoId);
        return ResponseEntity.ok(ApiResponse.success("Photo deleted successfully"));
    }

    @PatchMapping("/{photoId}/primary")
    @Operation(summary = "Set a photo as the primary profile photo")
    public ResponseEntity<ApiResponse<PhotoResponse>> setPrimary(
            @AuthenticationPrincipal User user,
            @PathVariable Long photoId) {
        PhotoResponse response = photoService.setPrimaryPhoto(user.getId(), photoId);
        return ResponseEntity.ok(ApiResponse.success("Primary photo updated", response));
    }

    @PatchMapping("/{photoId}/visibility")
    @Operation(summary = "Update photo visibility (PUBLIC, CONNECTIONS_ONLY, HIDDEN)")
    public ResponseEntity<ApiResponse<PhotoResponse>> updateVisibility(
            @AuthenticationPrincipal User user,
            @PathVariable Long photoId,
            @RequestParam("visibility") String visibility) {
        PhotoResponse response = photoService.updateVisibility(user.getId(), photoId, visibility);
        return ResponseEntity.ok(ApiResponse.success("Visibility updated", response));
    }
}
