package com.agrabandhan.search.repository;

import com.agrabandhan.search.entity.ProfileView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileViewRepository extends JpaRepository<ProfileView, Long> {

    Optional<ProfileView> findByViewerProfileIdAndViewedProfileId(Long viewerProfileId, Long viewedProfileId);

    @Query("SELECT pv FROM ProfileView pv " +
           "JOIN FETCH pv.viewerProfile vp " +
           "LEFT JOIN FETCH vp.photos " +
           "WHERE pv.viewedProfile.id = :profileId " +
           "ORDER BY pv.viewedAt DESC")
    Page<ProfileView> findWhoViewedMe(Long profileId, Pageable pageable);

    @Query("SELECT pv FROM ProfileView pv " +
           "JOIN FETCH pv.viewedProfile vd " +
           "LEFT JOIN FETCH vd.photos " +
           "WHERE pv.viewerProfile.id = :profileId " +
           "ORDER BY pv.viewedAt DESC")
    Page<ProfileView> findMyViews(Long profileId, Pageable pageable);

    long countByViewedProfileId(Long profileId);
}
