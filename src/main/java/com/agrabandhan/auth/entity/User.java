package com.agrabandhan.auth.entity;

import com.agrabandhan.common.entity.BaseEntity;
import com.agrabandhan.common.entity.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Column(name = "phone_number", nullable = false, unique = true, length = 15)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "is_profile_complete", nullable = false)
    @Builder.Default
    private boolean profileComplete = false;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "last_login_at")
    private java.time.LocalDateTime lastLoginAt;
}
