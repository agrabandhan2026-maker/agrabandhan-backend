package com.agrabandhan.community.repository;

import com.agrabandhan.community.entity.VerificationRequest;
import com.agrabandhan.community.entity.VerificationRequest.VerificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VerificationRequestRepository extends JpaRepository<VerificationRequest, Long> {
    Optional<VerificationRequest> findByProfileIdAndStatus(Long profileId, VerificationStatus status);
    Page<VerificationRequest> findByStatus(VerificationStatus status, Pageable pageable);
    boolean existsByProfileIdAndStatus(Long profileId, VerificationStatus status);
    long countByStatus(VerificationStatus status);
}
