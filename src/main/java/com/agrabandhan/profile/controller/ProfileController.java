package com.agrabandhan.profile.controller;

import com.agrabandhan.auth.entity.User;
import com.agrabandhan.common.dto.ApiResponse;
import com.agrabandhan.profile.dto.ProfileDto.*;
import com.agrabandhan.profile.service.ProfileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/profiles")
@RequiredArgsConstructor
@Tag(name = "Profile", description = "Profile creation and management APIs")
public class ProfileController {

    private final ProfileService profileService;

    // =============================================
    //  Step 1: Personal Details
    // =============================================
    @PostMapping("/personal")
    @Operation(summary = "Create profile with personal details (Step 1)")
    public ResponseEntity<ApiResponse<ProfileDetailResponse>> createPersonalDetails(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PersonalDetailsRequest request) {
        ProfileDetailResponse response = profileService.createPersonalDetails(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Personal details saved", response));
    }

    @PutMapping("/personal")
    @Operation(summary = "Update personal details")
    public ResponseEntity<ApiResponse<ProfileDetailResponse>> updatePersonalDetails(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PersonalDetailsRequest request) {
        ProfileDetailResponse response = profileService.updatePersonalDetails(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Personal details updated", response));
    }

    // =============================================
    //  Step 2: Family Details
    // =============================================
    @PostMapping("/family")
    @Operation(summary = "Save family details (Step 2)")
    public ResponseEntity<ApiResponse<ProfileDetailResponse>> saveFamilyDetails(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody FamilyDetailsRequest request) {
        ProfileDetailResponse response = profileService.saveFamilyDetails(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Family details saved", response));
    }

    // =============================================
    //  Step 3: Education Details
    // =============================================
    @PostMapping("/education")
    @Operation(summary = "Save education details (Step 3)")
    public ResponseEntity<ApiResponse<ProfileDetailResponse>> saveEducationDetails(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody EducationDetailsRequest request) {
        ProfileDetailResponse response = profileService.saveEducationDetails(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Education details saved", response));
    }

    // =============================================
    //  Step 4: Profession Details
    // =============================================
    @PostMapping("/profession")
    @Operation(summary = "Save profession details (Step 4)")
    public ResponseEntity<ApiResponse<ProfileDetailResponse>> saveProfessionDetails(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody ProfessionDetailsRequest request) {
        ProfileDetailResponse response = profileService.saveProfessionDetails(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Profession details saved", response));
    }

    // =============================================
    //  Step 5: Lifestyle Details
    // =============================================
    @PostMapping("/lifestyle")
    @Operation(summary = "Save lifestyle details (Step 5)")
    public ResponseEntity<ApiResponse<ProfileDetailResponse>> saveLifestyleDetails(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody LifestyleDetailsRequest request) {
        ProfileDetailResponse response = profileService.saveLifestyleDetails(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Lifestyle details saved", response));
    }

    // =============================================
    //  Get Profile
    // =============================================
    @GetMapping("/me")
    @Operation(summary = "Get my profile with all details")
    public ResponseEntity<ApiResponse<ProfileDetailResponse>> getMyProfile(
            @AuthenticationPrincipal User user) {
        ProfileDetailResponse response = profileService.getMyProfile(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/{profileId}")
    @Operation(summary = "Get profile by ID")
    public ResponseEntity<ApiResponse<ProfileDetailResponse>> getProfileById(
            @PathVariable Long profileId) {
        ProfileDetailResponse response = profileService.getProfileById(profileId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
