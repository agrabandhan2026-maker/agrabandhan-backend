package com.agrabandhan.community.repository;

import com.agrabandhan.community.entity.PhotoVerification;
import com.agrabandhan.community.entity.VerificationRequest.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PhotoVerificationRepository extends JpaRepository<PhotoVerification, Long> {
    Page<PhotoVerification> findByStatus(VerificationStatus status, Pageable pageable);
    boolean existsByProfileIdAndStatus(Long profileId, VerificationStatus status);
    long countByStatus(VerificationStatus status);
}
