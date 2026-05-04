package com.jobtracker.controller;

import com.jobtracker.dto.JobApplicationDto;
import com.jobtracker.model.ApplicationStatus;
import com.jobtracker.service.JobApplicationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@CrossOrigin(origins = "${cors.allowed-origins}")
public class JobApplicationController {

    private final JobApplicationService service;

    @GetMapping
    public ResponseEntity<List<JobApplicationDto.Response>> getAllApplications(
            @RequestParam(required = false) ApplicationStatus status) {
        if (status != null) {
            return ResponseEntity.ok(service.getApplicationsByStatus(status));
        }
        return ResponseEntity.ok(service.getAllApplications());
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationDto.Response> getApplication(@PathVariable Long id) {
        return ResponseEntity.ok(service.getApplicationById(id));
    }

    @PostMapping
    public ResponseEntity<JobApplicationDto.Response> createApplication(
            @Valid @RequestBody JobApplicationDto.CreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.createApplication(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobApplicationDto.Response> updateApplication(
            @PathVariable Long id,
            @RequestBody JobApplicationDto.UpdateRequest request) {
        return ResponseEntity.ok(service.updateApplication(id, request));
    }

    @PostMapping("/{id}/analyze")
    public ResponseEntity<JobApplicationDto.Response> analyzeApplication(@PathVariable Long id) {
        return ResponseEntity.ok(service.analyzeApplication(id));
    }

    @PostMapping("/analyze")
    public ResponseEntity<JobApplicationDto.AnalysisResult> quickAnalyze(
            @Valid @RequestBody JobApplicationDto.AnalyzeRequest request) {
        return ResponseEntity.ok(service.quickAnalyze(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteApplication(@PathVariable Long id) {
        service.deleteApplication(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats")
    public ResponseEntity<JobApplicationDto.StatsResponse> getStats() {
        return ResponseEntity.ok(service.getStats());
    }
}
