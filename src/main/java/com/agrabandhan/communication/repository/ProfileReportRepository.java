package com.agrabandhan.communication.repository;

import com.agrabandhan.communication.entity.ProfileReport;
import com.agrabandhan.communication.entity.ProfileReport.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfileReportRepository extends JpaRepository<ProfileReport, Long> {

    boolean existsByReporterProfileIdAndReportedProfileId(Long reporterId, Long reportedId);

    Page<ProfileReport> findByStatus(ReportStatus status, Pageable pageable);

    long countByStatus(ReportStatus status);
}
