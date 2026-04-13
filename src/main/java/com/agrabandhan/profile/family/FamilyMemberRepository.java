package com.agrabandhan.profile.family;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FamilyMemberRepository extends JpaRepository<FamilyMember, Long> {

    List<FamilyMember> findByProfileIdAndActiveTrue(Long profileId);

    Optional<FamilyMember> findByInviteToken(String inviteToken);

    Optional<FamilyMember> findByProfileIdAndUserId(Long profileId, Long userId);

    int countByProfileIdAndActiveTrue(Long profileId);

    boolean existsByProfileIdAndUserIdAndActiveTrue(Long profileId, Long userId);
}
