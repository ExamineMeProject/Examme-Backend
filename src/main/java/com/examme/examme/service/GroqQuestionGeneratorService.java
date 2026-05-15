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
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class GroqQuestionGeneratorService {

    @Value("${groq.api.key:}")
    private String groqApiKey;

    @Value("${groq.model:llama-3.1-8b-instant}")
    private String groqModel;

    @Value("${groq.url:https://api.groq.com/openai/v1/chat/completions}")
    private String groqUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public List<QuizQuestionResponseDto> generateFromLectureText(
            String extractedText,
            int questionCount,
            Difficulty difficulty,
            String teacherDescription) {
        
        log.info("Generating {} questions for difficulty {}. Extracted text length: {}", 
                questionCount, difficulty, extractedText.length());

        if (extractedText == null || extractedText.isBlank()) {
            log.warn("Extracted text is empty!");
            throw new IllegalArgumentException("Mühazirə mətni boşdur. Zəhmət olmasa faylı yoxlayın.");
        }

        String desc = teacherDescription == null || teacherDescription.isBlank() ? "" : teacherDescription;
        
        // Safety truncation to ensure we don't exceed Groq's free tier TPM limits
        String processedText = truncateText(extractedText, 15_000);
        
        // Improved prompt for better context adherence
        String prompt = """
                SƏN PROFESSIONAL BİR MÜƏLLİMSƏN. SƏNİN TAPŞIRIĞIN AŞAĞIDAKI MÜHAZİRƏ MƏTNİNİ DİQQƏTLƏ OXUMAQ VƏ ORADAKI FAKTLARA ƏSASLANARAQ TEST YARATMAQDIR.
                
                TAPŞIRIQLAR:
                1. Mətndən tam olaraq %d ədəd test sualı yarat.
                2. Səviyyə: %s
                3. Dil: Azərbaycan dili
                4. Əlavə təlimat: %s
                
                VACİB QAYDALAR:
                - Suallar mütləq mətndəki spesifik məlumatlara əsaslanmalıdır. 
                - "Mühazirə haqqında nə danışılır?" kimi ümumi suallar QƏTİYYƏN OLMAZ.
                - Cavabı YALNIZ JSON formatında ver.
                
                JSON FORMATI:
                [
                  {
                    "questionId": 1,
                    "question": "Sual mətni (mətndən spesifik bir fakt)",
                    "options": {
                      "A": "Variant 1",
                      "B": "Variant 2",
                      "C": "Variant 3",
                      "D": "Variant 4"
                    },
                    "correctAnswer": "Düzgün variantın hərfi (A, B, C və ya D)"
                  }
                ]
                
                ### MÜHAZİRƏ MƏTNİ START ###
                %s
                ### MÜHAZİRƏ MƏTNİ END ###
                """.formatted(questionCount, difficulty.name(), desc, processedText);

        String responseText = callGroq(prompt);
        return parseQuestions(responseText);
    }

    private String truncateText(String text, int maxChars) {
        if (text == null || text.length() <= maxChars) {
            return text;
        }
        log.warn("Mətn çox uzun olduğu üçün {} simvola qədər qısaldıldı.", maxChars);
        return text.substring(0, maxChars);
    }

    private String callGroq(String userPrompt) {
        try {
            if (groqApiKey == null || groqApiKey.isBlank()) {
                throw new IllegalStateException("Groq API key is not configured");
            }

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", groqModel);
            
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", "Sən professional test hazırlayan müəllimsən. Yalnız JSON formatında cavab verirsən."));
            messages.add(Map.of("role", "user", "content", userPrompt));
            
            requestBody.put("messages", messages);
            requestBody.put("temperature", 0.7);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(groqApiKey.trim());
            
            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);

            log.debug("Calling Groq API at {} with model {}", groqUrl, groqModel);
            String response = restTemplate.postForObject(groqUrl, entity, String.class);
            
            JsonNode root = objectMapper.readTree(response);
            return root.path("choices").get(0).path("message").path("content").asText();

        } catch (HttpClientErrorException e) {
            if (e.getStatusCode().value() == 429) {
                log.error("Groq API quota exceeded (429 or 413 TPM limit).", e);
                throw new IllegalStateException("Groq API limitləri keçildi (TPM limit). Zəhmət olmasa bir az gözləyin və ya daha qısa mətn yoxlayın.", e);
            }
            if (e.getStatusCode().value() == 413) {
                log.error("Groq API payload too large (413).", e);
                throw new IllegalStateException("Mətn çox böyükdür. Groq API limitlərinə görə mətn qısaldılmalıdır.", e);
            }
            if (e.getStatusCode().value() == 401) {
                log.error("Groq API unauthorized (401). Check your API key.", e);
                throw new IllegalStateException("Groq API key is invalid or unauthorized.", e);
            }
            log.error("Groq API client error: {}", e.getMessage(), e);
            throw new RuntimeException("Groq API xətası: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("Groq API error", e);
            throw new RuntimeException("Error generating questions using Groq API: " + e.getMessage(), e);
        }
    }

    private List<QuizQuestionResponseDto> parseQuestions(String response) {
        try {
            String json = extractJsonArray(response);
            QuizQuestionResponseDto[] arr = objectMapper.readValue(json, QuizQuestionResponseDto[].class);
            return List.of(arr);
        } catch (Exception e) {
            log.error("Failed to parse Groq JSON response: {}", response, e);
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
