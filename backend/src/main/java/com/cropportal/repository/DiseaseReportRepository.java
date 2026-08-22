package com.cropportal.repository;

import com.cropportal.entity.DiseaseReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiseaseReportRepository extends JpaRepository<DiseaseReport, Long> {
    Page<DiseaseReport> findByFarmerUserEmail(String email, Pageable pageable);
    long countByDiseaseName(String diseaseName);
}
