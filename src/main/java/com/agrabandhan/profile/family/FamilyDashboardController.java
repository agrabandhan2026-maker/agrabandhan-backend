package com.agrabandhan.profile.family;

import com.agrabandhan.auth.entity.User;
import com.agrabandhan.common.dto.ApiResponse;
import com.agrabandhan.common.dto.PagedResponse;
import com.agrabandhan.profile.family.FamilyDto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/family")
@RequiredArgsConstructor
@Tag(name = "Family Dashboard", description = "Multi-user family management, activity log, and voice introduction")
public class FamilyDashboardController {

    private final FamilyDashboardService familyService;

    // =============================================
    //  Dashboard
    // =============================================
    @GetMapping("/dashboard")
    @Operation(summary = "Get family dashboard (members, voice intro, counts)")
    public ResponseEntity<ApiResponse<FamilyDashboardResponse>> getDashboard(
            @AuthenticationPrincipal User user) {
        FamilyDashboardResponse dashboard = familyService.getDashboard(user.getId());
        return ResponseEntity.ok(ApiResponse.success(dashboard));
    }

    // =============================================
    //  Family Members
    // =============================================
    @PostMapping("/members")
    @Operation(summary = "Add a family member (max 4, generates invite token)")
    public ResponseEntity<ApiResponse<FamilyMemberResponse>> addMember(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody AddFamilyMemberRequest request) {
        FamilyMemberResponse response = familyService.addFamilyMember(user.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Family member added. Share the invite token.", response));
    }

    @PostMapping("/members/accept-invite")
    @Operation(summary = "Accept a family invite using the invite token")
    public ResponseEntity<ApiResponse<FamilyMemberResponse>> acceptInvite(
            @AuthenticationPrincipal User user,
            @RequestParam String token) {
        FamilyMemberResponse response = familyService.acceptInvite(user.getId(), token);
        return ResponseEntity.ok(ApiResponse.success("Invite accepted. You can now manage this profile.", response));
    }

    @PatchMapping("/members/{memberId}/permissions")
    @Operation(summary = "Update a family member's permissions")
    public ResponseEntity<ApiResponse<FamilyMemberResponse>> updatePermissions(
            @AuthenticationPrincipal User user,
            @PathVariable Long memberId,
            @RequestBody UpdatePermissionsRequest request) {
        FamilyMemberResponse response = familyService.updatePermissions(user.getId(), memberId, request);
        return ResponseEntity.ok(ApiResponse.success("Permissions updated", response));
    }

    @DeleteMapping("/members/{memberId}")
    @Operation(summary = "Remove a family member")
    public ResponseEntity<ApiResponse<Void>> removeMember(
            @AuthenticationPrincipal User user,
            @PathVariable Long memberId) {
        familyService.removeFamilyMember(user.getId(), memberId);
        return ResponseEntity.ok(ApiResponse.success("Family member removed"));
    }

    // =============================================
    //  Activity Log
    // =============================================
    @GetMapping("/activity")
    @Operation(summary = "Get profile activity log (who did what)")
    public ResponseEntity<ApiResponse<PagedResponse<ActivityLogResponse>>> getActivityLog(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<ActivityLogResponse> logs = familyService.getActivityLog(user.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(logs));
    }

    // =============================================
    //  Voice Introduction
    // =============================================
    @PostMapping(value = "/voice-intro", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a 60-second voice introduction (audio file, max 5MB)")
    public ResponseEntity<ApiResponse<VoiceIntroResponse>> uploadVoiceIntro(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "recordedByName", required = false) String recordedByName,
            @RequestParam(value = "relationship", required = false) String relationship) {
        VoiceIntroResponse response = familyService.uploadVoiceIntro(
                user.getId(), file, recordedByName, relationship);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Voice introduction uploaded", response));
    }

    @GetMapping("/voice-intro/{profileId}")
    @Operation(summary = "Get voice introduction for a profile")
    public ResponseEntity<ApiResponse<VoiceIntroResponse>> getVoiceIntro(
            @PathVariable Long profileId) {
        VoiceIntroResponse response = familyService.getVoiceIntro(profileId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/voice-intro")
    @Operation(summary = "Delete my voice introduction")
    public ResponseEntity<ApiResponse<Void>> deleteVoiceIntro(
            @AuthenticationPrincipal User user) {
        familyService.deleteVoiceIntro(user.getId());
        return ResponseEntity.ok(ApiResponse.success("Voice introduction deleted"));
    }
}
