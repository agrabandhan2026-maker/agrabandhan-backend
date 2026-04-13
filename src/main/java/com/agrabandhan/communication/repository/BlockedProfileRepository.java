package com.agrabandhan.communication.repository;

import com.agrabandhan.communication.entity.BlockedProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlockedProfileRepository extends JpaRepository<BlockedProfile, Long> {

    boolean existsByBlockerProfileIdAndBlockedProfileId(Long blockerId, Long blockedId);

    void deleteByBlockerProfileIdAndBlockedProfileId(Long blockerId, Long blockedId);

    @Query("SELECT bp.blockedProfile.id FROM BlockedProfile bp WHERE bp.blockerProfile.id = :profileId")
    List<Long> findBlockedProfileIds(Long profileId);

    @Query("SELECT bp.blockerProfile.id FROM BlockedProfile bp WHERE bp.blockedProfile.id = :profileId")
    List<Long> findBlockedByProfileIds(Long profileId);
}
