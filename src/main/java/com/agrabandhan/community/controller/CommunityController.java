package com.agrabandhan.community.controller;

import com.agrabandhan.auth.entity.User;
import com.agrabandhan.common.dto.ApiResponse;
import com.agrabandhan.common.dto.PagedResponse;
import com.agrabandhan.community.dto.CommunityDto.*;
import com.agrabandhan.community.service.CommunityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/community")
@RequiredArgsConstructor
@Tag(name = "Community & Trust", description = "Verification, endorsements, Samaj directory, trust score")
public class CommunityController {

    private final CommunityService communityService;

    // =============================================
    //  ID Verification
    // =============================================
    @PostMapping(value = "/verify/id", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Submit ID document for verification (Aadhaar/PAN/etc)")
    public ResponseEntity<ApiResponse<VerificationResponse>> submitIdVerification(
            @AuthenticationPrincipal User user,
            @RequestParam("document") MultipartFile document,
            @RequestParam(value = "selfie", required = false) MultipartFile selfie,
            @RequestParam("documentType") String documentType) {
        VerificationResponse response = communityService.submitIdVerification(
                user.getId(), document, selfie, documentType);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Verification submitted. Our team will review it within 24 hours.", response));
    }

    @PostMapping(value = "/verify/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Submit selfie for photo verification")
    public ResponseEntity<ApiResponse<VerificationResponse>> submitPhotoVerification(
            @AuthenticationPrincipal User user,
            @RequestParam("selfie") MultipartFile selfie) {
        VerificationResponse response = communityService.submitPhotoVerification(user.getId(), selfie);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Photo verification submitted", response));
    }

    // Admin: Review verification
    @PatchMapping("/verify/{requestId}/review")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Review a verification request (Admin/Moderator)")
    public ResponseEntity<ApiResponse<VerificationResponse>> reviewVerification(
            @AuthenticationPrincipal User user,
            @PathVariable Long requestId,
            @Valid @RequestBody ReviewVerificationRequest request) {
        VerificationResponse response = communityService.reviewVerification(user.getId(), requestId, request);
        return ResponseEntity.ok(ApiResponse.success("Verification reviewed", response));
    }

    // Admin: Get pending verifications
    @GetMapping("/verify/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @Operation(summary = "Get pending verification requests (Admin/Moderator)")
    public ResponseEntity<ApiResponse<Page<VerificationResponse>>> getPendingVerifications(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<VerificationResponse> pending = communityService.getPendingVerifications(page, size);
        return ResponseEntity.ok(ApiResponse.success(pending));
    }

    // =============================================
    //  Endorsements
    // =============================================
    @PostMapping("/endorse/{profileId}")
    @Operation(summary = "Endorse a profile with a testimonial (max 3 per profile)")
    public ResponseEntity<ApiResponse<EndorsementResponse>> endorseProfile(
            @AuthenticationPrincipal User user,
            @PathVariable Long profileId,
            @Valid @RequestBody EndorsementRequest request) {
        EndorsementResponse response = communityService.endorseProfile(user.getId(), profileId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Endorsement added", response));
    }

    @GetMapping("/endorsements/{profileId}")
    @Operation(summary = "Get endorsements for a profile")
    public ResponseEntity<ApiResponse<List<EndorsementResponse>>> getEndorsements(
            @PathVariable Long profileId) {
        List<EndorsementResponse> endorsements = communityService.getEndorsements(profileId);
        return ResponseEntity.ok(ApiResponse.success(endorsements));
    }

    // =============================================
    //  Trust Score
    // =============================================
    @GetMapping("/trust-score/{profileId}")
    @Operation(summary = "Get trust score with breakdown and badges")
    public ResponseEntity<ApiResponse<TrustScoreResponse>> getTrustScore(
            @PathVariable Long profileId) {
        TrustScoreResponse score = communityService.getTrustScore(profileId);
        return ResponseEntity.ok(ApiResponse.success(score));
    }

    // =============================================
    //  Mutual Connections
    // =============================================
    @GetMapping("/mutual/{profileId}")
    @Operation(summary = "Get mutual connections with a profile (shared Sabhas, endorsers)")
    public ResponseEntity<ApiResponse<MutualConnectionResponse>> getMutualConnections(
            @AuthenticationPrincipal User user,
            @PathVariable Long profileId) {
        MutualConnectionResponse response = communityService.getMutualConnections(user.getId(), profileId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // =============================================
    //  Samaj Sabha Directory
    // =============================================
    @PostMapping("/sabha")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new Samaj Sabha (Admin only)")
    public ResponseEntity<ApiResponse<SabhaResponse>> createSabha(
            @Valid @RequestBody CreateSabhaRequest request) {
        SabhaResponse response = communityService.createSabha(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Sabha created", response));
    }

    @GetMapping("/sabha/city/{city}")
    @Operation(summary = "Find Sabhas by city")
    public ResponseEntity<ApiResponse<List<SabhaResponse>>> getSabhasByCity(
            @AuthenticationPrincipal User user,
            @PathVariable String city) {
        List<SabhaResponse> sabhas = communityService.getSabhasByCity(city, user != null ? user.getId() : null);
        return ResponseEntity.ok(ApiResponse.success(sabhas));
    }

    @GetMapping("/sabha/state/{state}")
    @Operation(summary = "Find Sabhas by state")
    public ResponseEntity<ApiResponse<List<SabhaResponse>>> getSabhasByState(
            @AuthenticationPrincipal User user,
            @PathVariable String state) {
        List<SabhaResponse> sabhas = communityService.getSabhasByState(state, user != null ? user.getId() : null);
        return ResponseEntity.ok(ApiResponse.success(sabhas));
    }

    @PostMapping("/sabha/{sabhaId}/join")
    @Operation(summary = "Join a Samaj Sabha")
    public ResponseEntity<ApiResponse<Void>> joinSabha(
            @AuthenticationPrincipal User user,
            @PathVariable Long sabhaId) {
        communityService.joinSabha(user.getId(), sabhaId);
        return ResponseEntity.ok(ApiResponse.success("Joined Sabha successfully"));
    }
}
