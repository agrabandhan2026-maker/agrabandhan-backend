package com.agrabandhan.search.controller;

import com.agrabandhan.auth.entity.User;
import com.agrabandhan.common.dto.ApiResponse;
import com.agrabandhan.common.dto.PagedResponse;
import com.agrabandhan.profile.dto.ProfileDto.ProfileSummaryResponse;
import com.agrabandhan.search.dto.SearchDto.*;
import com.agrabandhan.search.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Profile search, views, and shortlist")
public class SearchController {

    private final SearchService searchService;

    // =============================================
    //  Search
    // =============================================
    @PostMapping
    @Operation(summary = "Advanced search with 20+ filters (gotra auto-excluded)")
    public ResponseEntity<ApiResponse<PagedResponse<ProfileSummaryResponse>>> search(
            @AuthenticationPrincipal User user,
            @RequestBody SearchRequest request) {
        PagedResponse<ProfileSummaryResponse> results = searchService.search(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    @GetMapping("/quick/{preset}")
    @Operation(summary = "Quick search presets (NEW_PROFILES, SAME_CITY, SAME_STATE, HIGH_MATCH, WITH_PHOTO)")
    public ResponseEntity<ApiResponse<PagedResponse<ProfileSummaryResponse>>> quickSearch(
            @AuthenticationPrincipal User user,
            @PathVariable QuickSearch preset,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<ProfileSummaryResponse> results = searchService.quickSearch(user.getId(), preset, page, size);
        return ResponseEntity.ok(ApiResponse.success(results));
    }

    // =============================================
    //  Profile Views
    // =============================================
    @PostMapping("/views/{profileId}")
    @Operation(summary = "Record a profile view (call when user opens a profile)")
    public ResponseEntity<ApiResponse<Void>> recordView(
            @AuthenticationPrincipal User user,
            @PathVariable Long profileId) {
        searchService.recordProfileView(user.getId(), profileId);
        return ResponseEntity.ok(ApiResponse.success("View recorded"));
    }

    @GetMapping("/views/who-viewed-me")
    @Operation(summary = "Get profiles that viewed my profile")
    public ResponseEntity<ApiResponse<PagedResponse<ProfileViewResponse>>> whoViewedMe(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<ProfileViewResponse> views = searchService.getWhoViewedMe(user.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(views));
    }

    @GetMapping("/views/my-history")
    @Operation(summary = "Get profiles I have viewed")
    public ResponseEntity<ApiResponse<PagedResponse<ProfileViewResponse>>> myViewHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<ProfileViewResponse> views = searchService.getMyViewHistory(user.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(views));
    }

    // =============================================
    //  Shortlist
    // =============================================
    @PostMapping("/shortlist/{profileId}")
    @Operation(summary = "Add a profile to shortlist")
    public ResponseEntity<ApiResponse<ShortlistResponse>> addToShortlist(
            @AuthenticationPrincipal User user,
            @PathVariable Long profileId) {
        ShortlistResponse response = searchService.addToShortlist(user.getId(), profileId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Profile shortlisted", response));
    }

    @DeleteMapping("/shortlist/{profileId}")
    @Operation(summary = "Remove a profile from shortlist")
    public ResponseEntity<ApiResponse<Void>> removeFromShortlist(
            @AuthenticationPrincipal User user,
            @PathVariable Long profileId) {
        searchService.removeFromShortlist(user.getId(), profileId);
        return ResponseEntity.ok(ApiResponse.success("Removed from shortlist"));
    }

    @GetMapping("/shortlist")
    @Operation(summary = "Get my shortlisted profiles")
    public ResponseEntity<ApiResponse<PagedResponse<ShortlistResponse>>> myShortlist(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<ShortlistResponse> shortlist = searchService.getMyShortlist(user.getId(), page, size);
        return ResponseEntity.ok(ApiResponse.success(shortlist));
    }
}
