package com.agrabandhan.community.repository;

import com.agrabandhan.community.entity.TrustScore;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TrustScoreRepository extends JpaRepository<TrustScore, Long> {
    Optional<TrustScore> findByProfileId(Long profileId);
}
