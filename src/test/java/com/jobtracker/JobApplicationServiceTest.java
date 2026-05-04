package com.jobtracker;

import com.jobtracker.dto.JobApplicationDto;
import com.jobtracker.model.ApplicationStatus;
import com.jobtracker.model.JobApplication;
import com.jobtracker.repository.JobApplicationRepository;
import com.jobtracker.service.AnthropicService;
import com.jobtracker.service.JobApplicationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository repository;

    @Mock
    private AnthropicService anthropicService;

    @InjectMocks
    private JobApplicationService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createApplication_withoutAnalysis_savesSuccessfully() {
        JobApplicationDto.CreateRequest request = new JobApplicationDto.CreateRequest();
        request.setCompanyName("Acme Corp");
        request.setJobTitle("Senior Java Developer");

        JobApplication saved = JobApplication.builder()
                .id(1L)
                .companyName("Acme Corp")
                .jobTitle("Senior Java Developer")
                .status(ApplicationStatus.SAVED)
                .build();

        when(repository.save(any())).thenReturn(saved);

        JobApplicationDto.Response response = service.createApplication(request);

        assertNotNull(response);
        assertEquals("Acme Corp", response.getCompanyName());
        assertEquals(ApplicationStatus.SAVED, response.getStatus());
        verify(anthropicService, never()).analyzeResumeFit(any(), any());
    }

    @Test
    void getAllApplications_returnsAllRecords() {
        when(repository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(
                JobApplication.builder().id(1L).companyName("Company A").jobTitle("Dev").status(ApplicationStatus.APPLIED).build(),
                JobApplication.builder().id(2L).companyName("Company B").jobTitle("Dev").status(ApplicationStatus.SAVED).build()
        ));

        List<JobApplicationDto.Response> results = service.getAllApplications();

        assertEquals(2, results.size());
    }

    @Test
    void deleteApplication_notFound_throwsException() {
        when(repository.existsById(999L)).thenReturn(false);
        assertThrows(RuntimeException.class, () -> service.deleteApplication(999L));
    }

    @Test
    void analyzeApplication_missingJobDescription_throwsException() {
        JobApplication app = JobApplication.builder()
                .id(1L)
                .companyName("Acme")
                .jobTitle("Dev")
                .status(ApplicationStatus.SAVED)
                .build();
        when(repository.findById(1L)).thenReturn(Optional.of(app));

        assertThrows(IllegalArgumentException.class, () -> service.analyzeApplication(1L));
    }
}
