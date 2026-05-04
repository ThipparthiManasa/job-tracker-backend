package com.jobtracker.dto;

import com.jobtracker.model.ApplicationStatus;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDateTime;

public class JobApplicationDto {

    @Data
    public static class CreateRequest {
        @NotBlank(message = "Company name is required")
        private String companyName;

        @NotBlank(message = "Job title is required")
        private String jobTitle;

        private String jobDescription;
        private String resumeText;
        private String jobUrl;
        private String location;
        private String salaryRange;
        private String notes;
    }

    @Data
    public static class UpdateRequest {
        private String companyName;
        private String jobTitle;
        private String jobDescription;
        private String resumeText;
        private String jobUrl;
        private String location;
        private String salaryRange;
        private String notes;
        private ApplicationStatus status;
        private LocalDateTime appliedAt;
        private LocalDateTime interviewAt;
    }

    @Data
    public static class AnalyzeRequest {
        @NotBlank(message = "Job description is required")
        private String jobDescription;

        @NotBlank(message = "Resume text is required")
        private String resumeText;
    }

    @Data
    public static class Response {
        private Long id;
        private String companyName;
        private String jobTitle;
        private String jobDescription;
        private String resumeText;
        private ApplicationStatus status;
        private String jobUrl;
        private String location;
        private String salaryRange;
        private Integer fitScore;
        private String fitSummary;
        private String missingKeywords;
        private String suggestedEdits;
        private String strengths;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime appliedAt;
        private LocalDateTime interviewAt;
    }

    @Data
    public static class AnalysisResult {
        private Integer fitScore;
        private String fitSummary;
        private String missingKeywords;
        private String suggestedEdits;
        private String strengths;
    }

    @Data
    public static class StatsResponse {
        private Long total;
        private Long saved;
        private Long applied;
        private Long phoneScreen;
        private Long interview;
        private Long offer;
        private Long rejected;
        private Long withdrawn;
        private Double averageFitScore;
    }
}
