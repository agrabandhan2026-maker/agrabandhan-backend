package com.agrabandhan.admin.controller;

import com.agrabandhan.admin.dto.AdminDto.*;
import com.agrabandhan.admin.service.AdminService;
import com.agrabandhan.common.dto.ApiResponse;
import com.agrabandhan.common.dto.PagedResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Admin", description = "Admin dashboard and user management")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/dashboard")
    @Operation(summary = "Get dashboard statistics")
    public ResponseEntity<ApiResponse<DashboardStats>> getDashboardStats() {
        DashboardStats stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    @GetMapping("/users")
    @Operation(summary = "Get all users with pagination and search")
    public ResponseEntity<ApiResponse<PagedResponse<UserResponse>>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<UserResponse> users = adminService.getUsers(search, page, size);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.from(users)));
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get user detail by ID")
    public ResponseEntity<ApiResponse<UserDetailResponse>> getUserById(@PathVariable Long userId) {
        UserDetailResponse user = adminService.getUserById(userId);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PatchMapping("/users/{userId}/role")
    @Operation(summary = "Update user role (USER, PREMIUM_USER, MODERATOR, ADMIN)")
    public ResponseEntity<ApiResponse<UserResponse>> updateRole(
            @PathVariable Long userId,
            @RequestBody RoleUpdateRequest request) {
        UserResponse user = adminService.updateUserRole(userId, request.getRole());
        return ResponseEntity.ok(ApiResponse.success("Role updated", user));
    }

    @PatchMapping("/users/{userId}/suspend")
    @Operation(summary = "Suspend a user account")
    public ResponseEntity<ApiResponse<UserResponse>> suspendUser(@PathVariable Long userId) {
        UserResponse user = adminService.suspendUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User suspended", user));
    }

    @PatchMapping("/users/{userId}/activate")
    @Operation(summary = "Activate a suspended user account")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable Long userId) {
        UserResponse user = adminService.activateUser(userId);
        return ResponseEntity.ok(ApiResponse.success("User activated", user));
    }
}
