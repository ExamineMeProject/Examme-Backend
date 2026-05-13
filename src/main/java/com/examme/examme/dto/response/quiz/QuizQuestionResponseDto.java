package com.examme.examme.dto.response.quiz;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestionResponseDto {
    private Integer questionId;
    private String question;

    @Builder.Default
    private Map<String, String> options = new LinkedHashMap<>();

    private String correctAnswer;

    @JsonProperty("options")
    public Map<String, String> getOptions() {
        return options;
    }
}