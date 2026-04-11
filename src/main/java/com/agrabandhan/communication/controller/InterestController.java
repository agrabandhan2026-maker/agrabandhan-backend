package com.agrabandhan.communication.controller;

import com.agrabandhan.auth.entity.User;
import com.agrabandhan.common.dto.ApiResponse;
import com.agrabandhan.common.dto.PagedResponse;
import com.agrabandhan.communication.service.InterestService;
import com.agrabandhan.matching.dto.MatchingDto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/interests")
@RequiredArgsConstructor
@Tag(name = "Interests", description = "Send, accept, decline, and manage interests")
public class InterestController {

    private final InterestService interestService;

    @PostMapping("/send/{profileId}")
    @Operation(summary = "Send interest to a profile (with optional message)")
    public ResponseEntity<ApiResponse<InterestResponse>> sendInterest(
            @AuthenticationPrincipal User user,
            @PathVariable Long profileId,
            @RequestBody(required = false) SendInterestRequest request) {
        InterestResponse response = interestService.sendInterest(user.getId(), profileId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Interest sent", response));
    }

    @PatchMapping("/{interestId}/accept")
    @Operation(summary = "Accept a received interest (unlocks chat)")
    public ResponseEntity<ApiResponse<InterestResponse>> acceptInterest(
            @AuthenticationPrincipal User user,
            @PathVariable Long interestId) {
        InterestResponse response = interestService.acceptInterest(user.getId(), interestId);
        return ResponseEntity.ok(ApiResponse.success("Interest accepted", response));
    }

    @PatchMapping("/{interestId}/decline")
    @Operation(summary = "Decline a received interest (with optional reason)")
    public ResponseEntity<ApiResponse<InterestResponse>> declineInterest(
            @AuthenticationPrincipal User user,
            @PathVariable Long interestId,
            @RequestBody(required = false) DeclineInterestRequest request) {
        InterestResponse response = interestService.declineInterest(user.getId(), interestId, request);
        return ResponseEntity.ok(ApiResponse.success("Interest declined", response));
    }

    @PatchMapping("/{interestId}/withdraw")
    @Operation(summary = "Withdraw a sent interest (only if still pending)")
    public ResponseEntity<ApiResponse<InterestResponse>> withdrawInterest(
            @AuthenticationPrincipal User user,
            @PathVariable Long interestId) {
        InterestResponse response = interestService.withdrawInterest(user.getId(), interestId);
        return ResponseEntity.ok(ApiResponse.success("Interest withdrawn", response));
    }

    @GetMapping("/sent")
    @Operation(summary = "Get interests I've sent (filter by status: PENDING, ACCEPTED, DECLINED, WITHDRAWN)")
    public ResponseEntity<ApiResponse<PagedResponse<InterestResponse>>> getSentInterests(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<InterestResponse> interests = interestService.getSentInterests(user.getId(), status, page, size);
        return ResponseEntity.ok(ApiResponse.success(interests));
    }

    @GetMapping("/received")
    @Operation(summary = "Get interests I've received (filter by status)")
    public ResponseEntity<ApiResponse<PagedResponse<InterestResponse>>> getReceivedInterests(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PagedResponse<InterestResponse> interests = interestService.getReceivedInterests(user.getId(), status, page, size);
        return ResponseEntity.ok(ApiResponse.success(interests));
    }

    @GetMapping("/counts")
    @Operation(summary = "Get interest counts (pending received, for notification badge)")
    public ResponseEntity<ApiResponse<InterestCountResponse>> getInterestCounts(
            @AuthenticationPrincipal User user) {
        InterestCountResponse counts = interestService.getInterestCounts(user.getId());
        return ResponseEntity.ok(ApiResponse.success(counts));
    }
}
