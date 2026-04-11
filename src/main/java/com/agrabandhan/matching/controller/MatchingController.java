package com.agrabandhan.matching.controller;

import com.agrabandhan.auth.entity.User;
import com.agrabandhan.common.dto.ApiResponse;
import com.agrabandhan.matching.dto.MatchingDto.*;
import com.agrabandhan.matching.service.MatchingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/matching")
@RequiredArgsConstructor
@Tag(name = "Matching", description = "Partner preferences and daily match recommendations")
public class MatchingController {

    private final MatchingService matchingService;

    @PostMapping("/preferences")
    @Operation(summary = "Save/update partner preferences")
    public ResponseEntity<ApiResponse<PartnerPreferenceResponse>> savePreferences(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody PartnerPreferenceRequest request) {
        PartnerPreferenceResponse response = matchingService.savePreferences(user.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Partner preferences saved", response));
    }

    @GetMapping("/preferences")
    @Operation(summary = "Get my partner preferences")
    public ResponseEntity<ApiResponse<PartnerPreferenceResponse>> getPreferences(
            @AuthenticationPrincipal User user) {
        PartnerPreferenceResponse response = matchingService.getPreferences(user.getId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/daily")
    @Operation(summary = "Get today's match recommendations (top 10, auto-generated if empty)")
    public ResponseEntity<ApiResponse<List<DailyMatchResponse>>> getTodayMatches(
            @AuthenticationPrincipal User user) {
        List<DailyMatchResponse> matches = matchingService.getTodayMatches(user.getId());
        return ResponseEntity.ok(ApiResponse.success(matches));
    }
}
