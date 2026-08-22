package com.cropportal.ai;

import com.cropportal.dto.TicketRequest;
import com.cropportal.entity.DiseaseReport;
import com.cropportal.entity.Farmer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiTicketAiSupportService implements TicketAiSupportService {
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(45);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();

    @Value("${app.ai.gemini.api-key:}")
    private String apiKey;

    @Value("${app.ai.gemini.model:gemini-1.5-flash}")
    private String model;

    @Value("${app.ai.gemini.endpoint:https://generativelanguage.googleapis.com/v1beta}")
    private String endpoint;

    @Override
    public String resolveTicket(Farmer farmer, TicketRequest request, DiseaseReport diseaseReport) {
        if (!StringUtils.hasText(apiKey)) {
            return fallbackRecommendation(farmer, request, diseaseReport);
        }

        try {
            HttpRequest httpRequest = HttpRequest.newBuilder()
                    .uri(buildUri())
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(farmer, request, diseaseReport)))
                    .build();
            HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Gemini ticket resolution failed with status {} and body {}", response.statusCode(), response.body());
                return fallbackRecommendation(farmer, request, diseaseReport);
            }
            String answer = extractAnswer(response.body());
            return StringUtils.hasText(answer) ? answer.trim() : fallbackRecommendation(farmer, request, diseaseReport);
        } catch (Exception ex) {
            log.warn("Gemini ticket resolution failed: {}", ex.getMessage());
            return fallbackRecommendation(farmer, request, diseaseReport);
        }
    }

    private URI buildUri() {
        String baseUrl = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return URI.create(baseUrl + "/models/" + model + ":generateContent?key="
                + URLEncoder.encode(apiKey, StandardCharsets.UTF_8));
    }

    private String buildRequestBody(Farmer farmer, TicketRequest request, DiseaseReport diseaseReport) throws Exception {
        Map<String, Object> payload = Map.of(
                "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt(farmer, request, diseaseReport))))),
                "generationConfig", Map.of(
                        "temperature", 0.25,
                        "topP", 0.85,
                        "maxOutputTokens", 1000
                )
        );
        return objectMapper.writeValueAsString(payload);
    }

    private String extractAnswer(String responseBody) throws Exception {
        JsonNode root = objectMapper.readTree(responseBody);
        return root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
    }

    private String prompt(Farmer farmer, TicketRequest request, DiseaseReport report) {
        return """
                You are the AI Support agronomist for a crop disease farmer portal.
                Resolve this support ticket directly. Give practical farmer-safe guidance.
                Do not mention doctors. Do not prescribe banned or exact high-risk chemical dosages.
                Use this structure:
                Diagnosis Summary:
                Do This Today:
                Do This After 3 Days:
                Avoid This:
                Treatment Plan:
                Fertilizer / Pesticide Suggestion:
                Prevention Steps:
                Follow-up Date:
                When To Escalate:

                Farmer location: %s
                Primary crop: %s
                Ticket title: %s
                Farmer description: %s
                Priority: %s
                Linked disease report: %s
                """.formatted(
                value(farmer.getFarmLocation()),
                value(farmer.getPrimaryCrop()),
                value(request.title()),
                value(request.description()),
                value(request.priority()),
                reportSummary(report)
        );
    }

    private String reportSummary(DiseaseReport report) {
        if (report == null) return "No linked disease report.";
        return "Disease=%s, confidence=%s, severity=%s, affectedArea=%s, treatment=%s, pesticides=%s, fertilizers=%s, prevention=%s"
                .formatted(value(report.getDiseaseName()), value(report.getConfidenceScore()), value(report.getSeverityLevel()),
                        value(report.getAffectedArea()), value(report.getTreatment()), value(report.getRecommendedPesticides()),
                        value(report.getRecommendedFertilizers()), value(report.getPreventionMeasures()));
    }

    private String fallbackRecommendation(Farmer farmer, TicketRequest request, DiseaseReport report) {
        String disease = report == null ? "the reported crop issue" : value(report.getDiseaseName());
        String severity = report == null ? "UNKNOWN" : value(report.getSeverityLevel());
        String treatment = report == null || !StringUtils.hasText(report.getTreatment())
                ? "Remove badly affected leaves, isolate suspicious plants where possible, and monitor new growth daily."
                : report.getTreatment();
        String fertilizer = report == null || !StringUtils.hasText(report.getRecommendedFertilizers())
                ? "Use a balanced fertilizer plan based on soil condition and avoid excess nitrogen."
                : report.getRecommendedFertilizers();
        String prevention = report == null || !StringUtils.hasText(report.getPreventionMeasures())
                ? "Improve airflow, avoid overhead irrigation, remove crop debris, and inspect leaves every 2-3 days."
                : report.getPreventionMeasures();

        return """
                Diagnosis Summary:
                Your ticket has been resolved by AI Support using the supplied details and linked disease report.

                Likely Issue:
                %s. Current severity: %s.

                Do This Today:
                1. Photograph affected leaves every day for comparison.
                2. Remove heavily infected plant material safely.
                3. Avoid overhead watering and keep the crop canopy ventilated.

                Do This After 3 Days:
                Check whether new leaf spots are reducing. If symptoms continue spreading, raise the status to UNDER_TREATMENT and follow local agriculture officer guidance.

                Avoid This:
                Do not apply unknown chemical mixes, do not exceed label instructions, and do not irrigate directly over diseased leaves.

                Treatment Plan:
                %s

                Fertilizer / Pesticide Suggestion:
                %s

                Prevention Steps:
                %s

                Follow-up Date:
                Review crop condition after 3 days and again after 7 days.

                When To Escalate:
                If symptoms spread quickly, fruit/stem damage appears, or the crop does not improve within 5-7 days, contact a local agriculture officer with this ticket report.
                """.formatted(disease, severity, treatment, fertilizer, prevention);
    }

    private String value(Object value) {
        return value == null ? "Not provided" : String.valueOf(value);
    }
}
