package com.cropportal.ai;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cropportal.dto.DiseasePredictionOption;
import com.cropportal.dto.DiseasePredictionResponse;
import com.cropportal.entity.SeverityLevel;
import com.cropportal.exception.AiPredictionException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.ai.provider", havingValue = "gemini")
public class GeminiVisionProvider implements DiseaseDetectionProvider {
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
    public DiseasePredictionResponse predict(MultipartFile image) {
        System.out.println("========================================");
System.out.println("File Name   : " + image.getOriginalFilename());
System.out.println("Content Type: " + image.getContentType());
System.out.println("File Size   : " + image.getSize());
System.out.println("Is Empty    : " + image.isEmpty());
System.out.println("========================================");
        if (!StringUtils.hasText(apiKey)) {
            throw new AiPredictionException("Gemini API key is not configured. Set GEMINI_API_KEY or use APP_AI_PROVIDER=mock.");
        }

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(buildUri())
                    .timeout(REQUEST_TIMEOUT)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(image)))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {

    System.out.println("========================================");
    System.out.println("STATUS : " + response.statusCode());
    System.out.println("BODY   : ");
    System.out.println(response.body());
    System.out.println("========================================");

    throw new RuntimeException(response.body());
}
            return toPrediction(response.body());
        }catch (IOException ex) {

    System.out.println("========== IO EXCEPTION ==========");
    ex.printStackTrace();
    throw new RuntimeException(ex);

} catch (InterruptedException ex) {

    Thread.currentThread().interrupt();
    throw new AiPredictionException("AI prediction was interrupted", ex);
}
    }

    @Override
    public String name() {
        return "gemini";
    }

    private URI buildUri() {
        String baseUrl = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
        return URI.create(baseUrl + "/models/" + model + ":generateContent?key="
                + URLEncoder.encode(apiKey, StandardCharsets.UTF_8));
    }

    private String buildRequestBody(MultipartFile image) throws IOException {
        Map<String, Object> payload = Map.of(
                "contents", List.of(Map.of("parts", List.of(
                        Map.of("text", prompt()),
                        Map.of("inline_data", Map.of(
                                "mime_type", image.getContentType(),
                                "data", Base64.getEncoder().encodeToString(image.getBytes())
                        ))
                ))),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "topP", 0.8,
                        "maxOutputTokens", 4096,
                        "responseMimeType", "application/json"
                )
        );
        return objectMapper.writeValueAsString(payload);
    }

    private DiseasePredictionResponse toPrediction(String responseBody) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(responseBody);
        String text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
        System.out.println("========== GEMINI RESPONSE ==========");
System.out.println(text);
System.out.println("=====================================");
        if (!StringUtils.hasText(text)) {
            throw new AiPredictionException("AI prediction response was empty. Please retry.");
        }
        GeminiDiseasePayload payload = objectMapper.readValue(stripMarkdownFence(text), GeminiDiseasePayload.class);
        GeminiPrediction primary = payload.primaryPrediction();
        if (primary == null || !StringUtils.hasText(primary.diseaseName())) {
            throw new AiPredictionException("AI prediction response did not include a disease name. Please retry.");
        }

        List<GeminiPrediction> rawPredictions = payload.predictions() == null || payload.predictions().isEmpty()
                ? List.of(primary)
                : payload.predictions();
        DiseasePredictionOption normalizedPrimary = normalize(primary);
        List<DiseasePredictionOption> predictions = rawPredictions.stream().map(this::normalize).toList();

        return new DiseasePredictionResponse(
                null,
                normalizedPrimary.diseaseName(),
                clampConfidence(normalizedPrimary.confidenceScore()),
                safe(normalizedPrimary.affectedArea(), payload.affectedArea()),
                normalizedPrimary.severityLevel(),
                safe(payload.confidenceExplanation(), explanation(normalizedPrimary, payload)),
                safe(payload.diseaseDescription(), "Disease details were not provided by the AI model."),
                safeList(payload.symptoms()),
                safeList(payload.causes()),
                safe(payload.treatment(), "Consult an agronomist for localized treatment guidance."),
                safe(payload.recommendedPesticides(), "Use locally approved pesticides only after expert confirmation."),
                safe(payload.recommendedFertilizers(), "Use a balanced fertilizer plan based on soil condition."),
                safe(payload.organicTreatment(), "Remove affected plant material and use approved organic controls where applicable."),
                payload.preventionMeasures() == null
        ? "Monitor crop regularly, improve airflow, and avoid prolonged leaf wetness."
        : String.join(", ", payload.preventionMeasures()),
                safe(payload.expectedRecoveryTime(), "Varies by crop condition and treatment timing."),
                predictions,
                null,
                name()
        );
    }

    private DiseasePredictionOption normalize(GeminiPrediction option) {
        return new DiseasePredictionOption(
                safe(option.diseaseName(), "Unknown crop condition"),
                clampConfidence(option.confidenceScore()),
                parseSeverity(option.severityLevel()),
                safe(option.affectedArea(), "Visible leaf area")
        );
    }

    private SeverityLevel parseSeverity(String value) {
        if (!StringUtils.hasText(value)) return SeverityLevel.MODERATE;
        try {
            return SeverityLevel.valueOf(value.trim().toUpperCase().replace(' ', '_'));
        } catch (IllegalArgumentException ex) {
            return SeverityLevel.MODERATE;
        }
    }

    private Double clampConfidence(Double value) {
        if (value == null || value.isNaN()) return 0.0;
        return Math.max(0.0, Math.min(1.0, value));
    }

    private String safe(String value, String fallback) {
        return StringUtils.hasText(value) ? value.trim() : fallback;
    }

    private List<String> safeList(List<String> values) {
        if (values == null) return List.of();
        return values.stream().filter(StringUtils::hasText).map(String::trim).toList();
    }

    private String stripMarkdownFence(String text) {
        String value = text.trim();
        if (value.startsWith("```")) {
            value = value.replaceFirst("^```(?:json)?", "").replaceFirst("```$", "").trim();
        }
        return value;
    }

    private String prompt() {
    return """
        Analyze this crop image.

        Return ONLY valid JSON.

        {
          "primaryPrediction": {
            "diseaseName": "",
            "confidenceScore": 0.0,
            "severityLevel": "LOW",
            "affectedArea": ""
          },
          "predictions": [
            {
              "diseaseName": "",
              "confidenceScore": 0.0,
              "severityLevel": "LOW",
              "affectedArea": ""
            }
          ],
          "diseaseDescription": "",
          "confidenceExplanation": "",
          "symptoms": [],
          "causes": [],
          "treatment": "",
          "preventionMeasures": []
        }

        Do not use markdown.
        Do not explain anything.
        Return only JSON.
        """;
}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiDiseasePayload(
            GeminiPrediction primaryPrediction,
            List<GeminiPrediction> predictions,
            String affectedArea,
            String diseaseDescription,
            String confidenceExplanation,
            List<String> symptoms,
            List<String> causes,
            String treatment,
            String recommendedPesticides,
            String recommendedFertilizers,
            String organicTreatment,
            List<String> preventionMeasures,
            String expectedRecoveryTime
    ) {
    }

    private String explanation(DiseasePredictionOption primary, GeminiDiseasePayload payload) {
        String symptoms = payload.symptoms() == null || payload.symptoms().isEmpty()
                ? "visible crop symptoms"
                : String.join(", ", payload.symptoms());
        return "The model matched " + symptoms + " with " + primary.diseaseName()
                + " and estimated " + Math.round(primary.confidenceScore() * 100) + "% confidence from the image.";
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record GeminiPrediction(
            String diseaseName,
            Double confidenceScore,
            String severityLevel,
            String affectedArea
    ) {
    }
}
