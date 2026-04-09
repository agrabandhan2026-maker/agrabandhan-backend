package com.agrabandhan.admin.service;

import com.agrabandhan.admin.dto.AdminDto.*;
import com.agrabandhan.auth.entity.User;
import com.agrabandhan.auth.repository.UserRepository;
import com.agrabandhan.common.entity.Role;
import com.agrabandhan.common.exception.BadRequestException;
import com.agrabandhan.common.exception.ResourceNotFoundException;
import com.agrabandhan.profile.entity.Profile;
import com.agrabandhan.profile.repository.ProfileRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public DashboardStats getDashboardStats() {
        long totalUsers = userRepository.count();

        // Active today (logged in within last 24 hours)
        long activeToday = ((Number) entityManager.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.lastLoginAt > :since")
                .setParameter("since", LocalDateTime.now().minusDays(1))
                .getSingleResult()).longValue();

        // New this week
        long newThisWeek = ((Number) entityManager.createQuery(
                "SELECT COUNT(u) FROM User u WHERE u.createdAt > :since")
                .setParameter("since", LocalDateTime.now().minusDays(7))
                .getSingleResult()).longValue();

        // Profiles with completeness > 50
        long completeProfiles = ((Number) entityManager.createQuery(
                "SELECT COUNT(p) FROM Profile p WHERE p.completenessScore >= 50")
                .getSingleResult()).longValue();

        // Total profile views
        long totalViews = ((Number) entityManager.createQuery(
                "SELECT COUNT(pv) FROM ProfileView pv")
                .getSingleResult()).longValue();

        // Total shortlists
        long totalShortlists = ((Number) entityManager.createQuery(
                "SELECT COUNT(sp) FROM ShortlistedProfile sp")
                .getSingleResult()).longValue();

        return DashboardStats.builder()
                .totalUsers(totalUsers)
                .activeToday(activeToday)
                .newThisWeek(newThisWeek)
                .totalMatches(completeProfiles)
                .interestsSent(totalViews) // Using views as proxy until interests are built
                .interestsAccepted(totalShortlists) // Using shortlists as proxy
                .build();
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> getUsers(String search, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, Math.min(size, 100), Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<User> users;
        if (search != null && !search.isBlank()) {
            users = userRepository.findByPhoneNumberContaining(search, pageRequest);
        } else {
            users = userRepository.findAll(pageRequest);
        }

        return users.map(UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserDetailResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        Profile profile = profileRepository.findByUserId(userId).orElse(null);

        return UserDetailResponse.from(user, profile);
    }

    @Transactional
    public UserResponse updateUserRole(Long userId, String roleName) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        try {
            Role role = Role.valueOf(roleName.toUpperCase());
            user.setRole(role);
            user = userRepository.save(user);
            log.info("User role updated: userId={}, newRole={}", userId, role);
            return UserResponse.from(user);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role: " + roleName + ". Valid roles: USER, PREMIUM_USER, MODERATOR, ADMIN");
        }
    }

    @Transactional
    public UserResponse suspendUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        if (user.getRole() == Role.ADMIN) {
            throw new BadRequestException("Cannot suspend an admin user");
        }

        user.setActive(false);
        user = userRepository.save(user);
        log.info("User suspended: userId={}", userId);
        return UserResponse.from(user);
    }

    @Transactional
    public UserResponse activateUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));

        user.setActive(true);
        user = userRepository.save(user);
        log.info("User activated: userId={}", userId);
        return UserResponse.from(user);
    }
}
