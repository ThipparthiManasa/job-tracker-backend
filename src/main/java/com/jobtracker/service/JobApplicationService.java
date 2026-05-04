package com.jobtracker.service;

import com.jobtracker.dto.JobApplicationDto;
import com.jobtracker.model.ApplicationStatus;
import com.jobtracker.model.JobApplication;
import com.jobtracker.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JobApplicationService {

    private final JobApplicationRepository repository;
    private final AnthropicService anthropicService;

    public List<JobApplicationDto.Response> getAllApplications() {
        return repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<JobApplicationDto.Response> getApplicationsByStatus(ApplicationStatus status) {
        return repository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public JobApplicationDto.Response getApplicationById(Long id) {
        JobApplication app = findOrThrow(id);
        return toResponse(app);
    }

    @Transactional
    public JobApplicationDto.Response createApplication(JobApplicationDto.CreateRequest request) {
        JobApplication app = JobApplication.builder()
                .companyName(request.getCompanyName())
                .jobTitle(request.getJobTitle())
                .jobDescription(request.getJobDescription())
                .resumeText(request.getResumeText())
                .jobUrl(request.getJobUrl())
                .location(request.getLocation())
                .salaryRange(request.getSalaryRange())
                .notes(request.getNotes())
                .status(ApplicationStatus.SAVED)
                .build();

        // Auto-analyze if both job description and resume are provided
        if (request.getJobDescription() != null && !request.getJobDescription().isBlank()
                && request.getResumeText() != null && !request.getResumeText().isBlank()) {
            enrichWithAiAnalysis(app, request.getJobDescription(), request.getResumeText());
        }

        return toResponse(repository.save(app));
    }

    @Transactional
    public JobApplicationDto.Response updateApplication(Long id, JobApplicationDto.UpdateRequest request) {
        JobApplication app = findOrThrow(id);

        if (request.getCompanyName() != null) app.setCompanyName(request.getCompanyName());
        if (request.getJobTitle() != null) app.setJobTitle(request.getJobTitle());
        if (request.getJobDescription() != null) app.setJobDescription(request.getJobDescription());
        if (request.getResumeText() != null) app.setResumeText(request.getResumeText());
        if (request.getJobUrl() != null) app.setJobUrl(request.getJobUrl());
        if (request.getLocation() != null) app.setLocation(request.getLocation());
        if (request.getSalaryRange() != null) app.setSalaryRange(request.getSalaryRange());
        if (request.getNotes() != null) app.setNotes(request.getNotes());
        if (request.getStatus() != null) app.setStatus(request.getStatus());
        if (request.getAppliedAt() != null) app.setAppliedAt(request.getAppliedAt());
        if (request.getInterviewAt() != null) app.setInterviewAt(request.getInterviewAt());

        return toResponse(repository.save(app));
    }

    @Transactional
    public JobApplicationDto.Response analyzeApplication(Long id) {
        JobApplication app = findOrThrow(id);

        if (app.getJobDescription() == null || app.getJobDescription().isBlank()) {
            throw new IllegalArgumentException("Job description is required for analysis.");
        }
        if (app.getResumeText() == null || app.getResumeText().isBlank()) {
            throw new IllegalArgumentException("Resume text is required for analysis.");
        }

        enrichWithAiAnalysis(app, app.getJobDescription(), app.getResumeText());
        return toResponse(repository.save(app));
    }

    @Transactional
    public JobApplicationDto.AnalysisResult quickAnalyze(JobApplicationDto.AnalyzeRequest request) {
        return anthropicService.analyzeResumeFit(request.getJobDescription(), request.getResumeText());
    }

    @Transactional
    public void deleteApplication(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Application not found with id: " + id);
        }
        repository.deleteById(id);
    }

    public JobApplicationDto.StatsResponse getStats() {
        List<JobApplication> all = repository.findAll();

        JobApplicationDto.StatsResponse stats = new JobApplicationDto.StatsResponse();
        stats.setTotal((long) all.size());
        stats.setSaved(all.stream().filter(a -> a.getStatus() == ApplicationStatus.SAVED).count());
        stats.setApplied(all.stream().filter(a -> a.getStatus() == ApplicationStatus.APPLIED).count());
        stats.setPhoneScreen(all.stream().filter(a -> a.getStatus() == ApplicationStatus.PHONE_SCREEN).count());
        stats.setInterview(all.stream().filter(a -> a.getStatus() == ApplicationStatus.INTERVIEW).count());
        stats.setOffer(all.stream().filter(a -> a.getStatus() == ApplicationStatus.OFFER).count());
        stats.setRejected(all.stream().filter(a -> a.getStatus() == ApplicationStatus.REJECTED).count());
        stats.setWithdrawn(all.stream().filter(a -> a.getStatus() == ApplicationStatus.WITHDRAWN).count());

        double avgScore = all.stream()
                .filter(a -> a.getFitScore() != null)
                .mapToInt(JobApplication::getFitScore)
                .average()
                .orElse(0.0);
        stats.setAverageFitScore(Math.round(avgScore * 10.0) / 10.0);

        return stats;
    }

    // --- Helpers ---

    private void enrichWithAiAnalysis(JobApplication app, String jobDesc, String resumeText) {
        try {
            JobApplicationDto.AnalysisResult analysis = anthropicService.analyzeResumeFit(jobDesc, resumeText);
            app.setFitScore(analysis.getFitScore());
            app.setFitSummary(analysis.getFitSummary());
            app.setStrengths(analysis.getStrengths());
            app.setMissingKeywords(analysis.getMissingKeywords());
            app.setSuggestedEdits(analysis.getSuggestedEdits());
        } catch (Exception e) {
            log.warn("AI analysis failed, saving without analysis: {}", e.getMessage());
        }
    }

    private JobApplication findOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Application not found with id: " + id));
    }

    private JobApplicationDto.Response toResponse(JobApplication app) {
        JobApplicationDto.Response response = new JobApplicationDto.Response();
        response.setId(app.getId());
        response.setCompanyName(app.getCompanyName());
        response.setJobTitle(app.getJobTitle());
        response.setJobDescription(app.getJobDescription());
        response.setResumeText(app.getResumeText());
        response.setStatus(app.getStatus());
        response.setJobUrl(app.getJobUrl());
        response.setLocation(app.getLocation());
        response.setSalaryRange(app.getSalaryRange());
        response.setFitScore(app.getFitScore());
        response.setFitSummary(app.getFitSummary());
        response.setMissingKeywords(app.getMissingKeywords());
        response.setSuggestedEdits(app.getSuggestedEdits());
        response.setStrengths(app.getStrengths());
        response.setNotes(app.getNotes());
        response.setCreatedAt(app.getCreatedAt());
        response.setUpdatedAt(app.getUpdatedAt());
        response.setAppliedAt(app.getAppliedAt());
        response.setInterviewAt(app.getInterviewAt());
        return response;
    }
}
