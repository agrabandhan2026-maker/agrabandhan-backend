package com.agrabandhan.community.repository;

import com.agrabandhan.community.entity.ProfileSabhaLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProfileSabhaLinkRepository extends JpaRepository<ProfileSabhaLink, Long> {
    List<ProfileSabhaLink> findByProfileId(Long profileId);
    boolean existsByProfileIdAndSabhaId(Long profileId, Long sabhaId);
}
