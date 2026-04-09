package com.agrabandhan.search.repository;

import com.agrabandhan.search.entity.ShortlistedProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortlistedProfileRepository extends JpaRepository<ShortlistedProfile, Long> {

    Optional<ShortlistedProfile> findByProfileIdAndShortlistedProfileId(Long profileId, Long shortlistedId);

    boolean existsByProfileIdAndShortlistedProfileId(Long profileId, Long shortlistedId);

    @Query("SELECT sp FROM ShortlistedProfile sp " +
           "JOIN FETCH sp.shortlistedProfile p " +
           "LEFT JOIN FETCH p.photos " +
           "WHERE sp.profile.id = :profileId " +
           "ORDER BY sp.createdAt DESC")
    Page<ShortlistedProfile> findMyShortlist(Long profileId, Pageable pageable);

    long countByProfileId(Long profileId);

    void deleteByProfileIdAndShortlistedProfileId(Long profileId, Long shortlistedId);
}
