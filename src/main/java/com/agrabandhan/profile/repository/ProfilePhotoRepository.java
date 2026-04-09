package com.agrabandhan.profile.repository;

import com.agrabandhan.profile.entity.ProfilePhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProfilePhotoRepository extends JpaRepository<ProfilePhoto, Long> {

    List<ProfilePhoto> findByProfileIdOrderByDisplayOrderAsc(Long profileId);

    int countByProfileId(Long profileId);

    @Modifying
    @Query("UPDATE ProfilePhoto p SET p.primary = false WHERE p.profile.id = :profileId")
    void clearPrimaryFlag(Long profileId);
}
