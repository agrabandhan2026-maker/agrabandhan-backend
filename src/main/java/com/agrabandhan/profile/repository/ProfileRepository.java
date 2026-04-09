package com.agrabandhan.profile.repository;

import com.agrabandhan.profile.entity.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Long> {

    Optional<Profile> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    @Query("SELECT p FROM Profile p " +
           "LEFT JOIN FETCH p.familyDetail " +
           "LEFT JOIN FETCH p.educationDetail " +
           "LEFT JOIN FETCH p.professionDetail " +
           "LEFT JOIN FETCH p.lifestyleDetail " +
           "LEFT JOIN FETCH p.photos " +
           "WHERE p.id = :profileId")
    Optional<Profile> findByIdWithAllDetails(Long profileId);

    @Query("SELECT p FROM Profile p " +
           "LEFT JOIN FETCH p.familyDetail " +
           "LEFT JOIN FETCH p.educationDetail " +
           "LEFT JOIN FETCH p.professionDetail " +
           "LEFT JOIN FETCH p.lifestyleDetail " +
           "LEFT JOIN FETCH p.photos " +
           "WHERE p.user.id = :userId")
    Optional<Profile> findByUserIdWithAllDetails(Long userId);
}
