package com.agrabandhan.matching.repository;

import com.agrabandhan.matching.entity.PartnerPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PartnerPreferenceRepository extends JpaRepository<PartnerPreference, Long> {

    Optional<PartnerPreference> findByProfileId(Long profileId);

    boolean existsByProfileId(Long profileId);
}
