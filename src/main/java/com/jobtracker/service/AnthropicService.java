package com.jobtracker.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtracker.dto.JobApplicationDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnthropicService {

    @Value("${anthropic.api.key}")
    private String apiKey;

    @Value("${anthropic.api.url}")
    private String apiUrl;

    @Value("${anthropic.api.model}")
    private String model;

    private final ObjectMapper objectMapper;

    public JobApplicationDto.AnalysisResult analyzeResumeFit(String jobDescription, String resumeText) {
        String prompt = buildAnalysisPrompt(jobDescription, resumeText);

        String responseText = callAnthropicApi(prompt);
        return parseAnalysisResponse(responseText);
    }

    private String buildAnalysisPrompt(String jobDescription, String resumeText) {
        return """
                You are an expert technical recruiter and career coach specializing in software engineering roles.
                
                Analyze how well the candidate's resume matches the job description and provide a detailed assessment.
                
                JOB DESCRIPTION:
                %s
                
                CANDIDATE RESUME:
                %s
                
                Provide your analysis in the following JSON format ONLY (no other text):
                {
                  "fitScore": <integer 0-100>,
                  "fitSummary": "<2-3 sentence overall assessment of the candidate's fit>",
                  "strengths": "<bullet points of key matching strengths, separated by |>",
                  "missingKeywords": "<comma-separated list of important keywords/skills from the job description missing from the resume>",
                  "suggestedEdits": "<specific actionable suggestions to improve the resume for this role, separated by |>"
                }
                
                Scoring guide:
                - 80-100: Excellent match, highly qualified
                - 60-79: Good match, meets most requirements
                - 40-59: Partial match, meets some requirements
                - 0-39: Poor match, significant gaps
                """.formatted(jobDescription, resumeText);
    }

    private String callAnthropicApi(String prompt) {
        WebClient client = WebClient.builder()
                .baseUrl(apiUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "max_tokens", 1500,
                "messages", List.of(
                        Map.of("role", "user", "content", prompt)
                )
        );

        try {
            String response = client.post()
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            JsonNode root = objectMapper.readTree(response);
            return root.path("content").get(0).path("text").asText();
        } catch (Exception e) {
            log.error("Error calling Anthropic API: {}", e.getMessage());
            throw new RuntimeException("Failed to analyze resume. Please try again.", e);
        }
    }

    private JobApplicationDto.AnalysisResult parseAnalysisResponse(String responseText) {
        try {
            // Strip markdown code blocks if present
            String cleaned = responseText
                    .replaceAll("```json\\s*", "")
                    .replaceAll("```\\s*", "")
                    .trim();

            JsonNode json = objectMapper.readTree(cleaned);

            JobApplicationDto.AnalysisResult result = new JobApplicationDto.AnalysisResult();
            result.setFitScore(json.path("fitScore").asInt());
            result.setFitSummary(json.path("fitSummary").asText());
            result.setStrengths(json.path("strengths").asText());
            result.setMissingKeywords(json.path("missingKeywords").asText());
            result.setSuggestedEdits(json.path("suggestedEdits").asText());
            return result;
        } catch (Exception e) {
            log.error("Error parsing AI response: {}", responseText);
            throw new RuntimeException("Failed to parse AI analysis response.", e);
        }
    }
}
