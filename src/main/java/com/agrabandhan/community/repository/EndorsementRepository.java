package com.agrabandhan.community.repository;

import com.agrabandhan.community.entity.CommunityEndorsement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface EndorsementRepository extends JpaRepository<CommunityEndorsement, Long> {
    List<CommunityEndorsement> findByProfileIdAndApprovedTrue(Long profileId);
    boolean existsByProfileIdAndEndorserProfileId(Long profileId, Long endorserId);
    int countByProfileIdAndApprovedTrue(Long profileId);
}
