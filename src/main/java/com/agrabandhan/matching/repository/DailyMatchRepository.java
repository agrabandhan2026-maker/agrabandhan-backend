package com.agrabandhan.matching.repository;

import com.agrabandhan.matching.entity.DailyMatch;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyMatchRepository extends JpaRepository<DailyMatch, Long> {

    @Query("SELECT dm FROM DailyMatch dm " +
           "JOIN FETCH dm.matchedProfile mp " +
           "LEFT JOIN FETCH mp.photos " +
           "LEFT JOIN FETCH mp.educationDetail " +
           "LEFT JOIN FETCH mp.professionDetail " +
           "WHERE dm.profile.id = :profileId AND dm.matchDate = :date " +
           "ORDER BY dm.compatibilityScore DESC")
    List<DailyMatch> findTodayMatches(Long profileId, LocalDate date);

    Page<DailyMatch> findByProfileIdAndMatchDateOrderByCompatibilityScoreDesc(
            Long profileId, LocalDate matchDate, Pageable pageable);

    @Modifying
    @Query("DELETE FROM DailyMatch dm WHERE dm.matchDate < :beforeDate")
    void deleteOldMatches(LocalDate beforeDate);

    boolean existsByProfileIdAndMatchDate(Long profileId, LocalDate matchDate);
}
