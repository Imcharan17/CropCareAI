package com.cropportal.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslationService {
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    @Value("${app.ai.gemini.api-key:}")
    private String apiKey;

    @Value("${app.ai.gemini.model:gemini-2.5-flash}")
    private String model;

    @Value("${app.ai.gemini.endpoint:https://generativelanguage.googleapis.com/v1beta}")
    private String endpoint;

    public List<String> translate(List<String> texts, String language) {
        if (texts.stream().mapToInt(String::length).sum() > 12000) {
            throw new IllegalArgumentException("Translate at most 12,000 characters at a time.");
        }
        if (!StringUtils.hasText(apiKey)) {
            throw new IllegalStateException("Translation is unavailable because the Gemini API key is not configured.");
        }
        try {
            String languageName = "te".equals(language) ? "Telugu" : "Hindi";
            String prompt = "Translate each string in the JSON array into " + languageName + ". "
                    + "Return only a JSON object with a translations array in the same order and same length. "
                    + "Preserve line breaks and meaning, keep crop disease/scientific names and numbers accurate, "
                    + "and do not add advice or commentary. Input JSON array: " + objectMapper.writeValueAsString(texts);
            Map<String, Object> payload = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                    "generationConfig", Map.of("temperature", 0.1, "maxOutputTokens", 4096, "responseMimeType", "application/json")
            );
            String baseUrl = endpoint.endsWith("/") ? endpoint.substring(0, endpoint.length() - 1) : endpoint;
            URI uri = URI.create(baseUrl + "/models/" + model + ":generateContent?key="
                    + URLEncoder.encode(apiKey, StandardCharsets.UTF_8));
            HttpRequest request = HttpRequest.newBuilder().uri(uri).timeout(Duration.ofSeconds(45))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload))).build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Gemini translation failed with status {}", response.statusCode());
                throw new IllegalStateException("Translation provider returned an error.");
            }
            JsonNode root = objectMapper.readTree(response.body());
            String content = root.path("candidates").path(0).path("content").path("parts").path(0).path("text").asText();
            JsonNode values = objectMapper.readTree(content).path("translations");
            if (!values.isArray() || values.size() != texts.size()) {
                throw new IllegalStateException("Translation provider returned an incomplete response.");
            }
            return java.util.stream.IntStream.range(0, texts.size())
                    .mapToObj(index -> values.get(index).asText(texts.get(index))).toList();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Translation was interrupted.", ex);
        } catch (Exception ex) {
            if (ex instanceof IllegalStateException illegalStateException) throw illegalStateException;
            log.warn("Gemini translation failed: {}", ex.getMessage());
            throw new IllegalStateException("Translation is temporarily unavailable.", ex);
        }
    }
}
