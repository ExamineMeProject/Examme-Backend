package com.examme.examme.service;

import com.examme.examme.dto.response.quiz.QuizQuestionResponseDto;
import com.examme.examme.entity.enums.Difficulty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeminiQuestionGeneratorService {

    @Value("${gemini.api.key:}")
    private String geminiApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public List<QuizQuestionResponseDto> generateFromLectureText(
            String extractedText,
            int questionCount,
            Difficulty difficulty,
            String teacherDescription
    ) {
        if (geminiApiKey == null || geminiApiKey.isBlank()) {
            throw new IllegalStateException("Gemini API key is not configured");
        }
        String desc = teacherDescription == null || teacherDescription.isBlank() ? "" : teacherDescription;
        String prompt = """
                Sen bir müəllim köməkçisisən. Aşağıdakı mühazirə mətnindən %d ədəd %s səviyyəli test sualı yarat.
                Əlavə təlimat: %s
                Cavabı YALNIZ aşağıdakı JSON formatında ver, başqa heç nə yazma:
                [
                  {
                    "questionId": 1,
                    "question": "Sual mətni",
                    "options": {
                      "A": "...",
                      "B": "...",
                      "C": "...",
                      "D": "..."
                    },
                    "correctAnswer": "A"
                  }
                ]
                Mətn: %s
                """.formatted(questionCount, difficulty.name(), desc, extractedText);

        String responseText = callGemini(prompt);
        return parseQuestions(responseText);
    }

    private String callGemini(String userPrompt) {
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key="
                    + geminiApiKey;

            Map<String, Object> requestBody = new HashMap<>();
            Map<String, Object> content = new HashMap<>();
            List<Map<String, Object>> parts = new ArrayList<>();
            Map<String, Object> part = new HashMap<>();
            part.put("text", userPrompt);
            parts.add(part);
            content.put("parts", parts);
            requestBody.put("contents", List.of(content));
            requestBody.put("generationConfig", Map.of("responseMimeType", "application/json"));

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            String response = restTemplate.postForObject(url, entity, String.class);
            JsonNode root = objectMapper.readTree(response);
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();
        } catch (Exception e) {
            log.error("Gemini API error", e);
            throw new RuntimeException("Error generating questions using Gemini API", e);
        }
    }

    private List<QuizQuestionResponseDto> parseQuestions(String response) {
        try {
            String json = extractJsonArray(response);
            QuizQuestionResponseDto[] arr = objectMapper.readValue(json, QuizQuestionResponseDto[].class);
            return List.of(arr);
        } catch (Exception e) {
            log.error("Failed to parse Gemini JSON", e);
            throw new RuntimeException("Error parsing generated quiz questions", e);
        }
    }

    private String extractJsonArray(String response) {
        int start = response.indexOf('[');
        int end = response.lastIndexOf(']');
        if (start >= 0 && end > start) {
            return response.substring(start, end + 1);
        }
        return response;
    }
}
