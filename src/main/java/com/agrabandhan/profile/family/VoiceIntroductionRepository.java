package com.agrabandhan.profile.family;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VoiceIntroductionRepository extends JpaRepository<VoiceIntroduction, Long> {

    Optional<VoiceIntroduction> findByProfileIdAndActiveTrue(Long profileId);

    boolean existsByProfileIdAndActiveTrue(Long profileId);
}
