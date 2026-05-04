package com.jobtracker.repository;

import com.jobtracker.model.ApplicationStatus;
import com.jobtracker.model.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    List<JobApplication> findAllByOrderByCreatedAtDesc();

    List<JobApplication> findByStatusOrderByCreatedAtDesc(ApplicationStatus status);

    List<JobApplication> findByCompanyNameContainingIgnoreCaseOrderByCreatedAtDesc(String companyName);

    @Query("SELECT ja FROM JobApplication ja WHERE ja.fitScore IS NOT NULL ORDER BY ja.fitScore DESC")
    List<JobApplication> findAllWithScoreOrderByScoreDesc();

    @Query("SELECT COUNT(ja) FROM JobApplication ja WHERE ja.status = :status")
    Long countByStatus(ApplicationStatus status);
}
