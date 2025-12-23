package com.example.backend.domain.repository;

import com.example.backend.domain.entity.Report;
import com.example.backend.domain.entity.Report.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {
    
    Page<Report> findByStatus(ReportStatus status, Pageable pageable);
    
    Page<Report> findByReporterId(Long reporterId, Pageable pageable);
}

