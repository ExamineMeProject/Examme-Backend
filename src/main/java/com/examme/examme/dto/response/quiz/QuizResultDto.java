package com.examme.examme.dto.response.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizResultDto {
    private int totalQuestions;
    private int correct;
    private int wrong;
    private int skipped;
    private double score;
    private List<QuestionResultDetailDto> details;
}
