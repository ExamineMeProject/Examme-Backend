package com.examme.examme.dto.request.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSubmitAnswerDto {
    private int questionId;
    private String selectedAnswer;
}
