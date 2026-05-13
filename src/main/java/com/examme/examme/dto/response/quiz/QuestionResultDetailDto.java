package com.examme.examme.dto.response.quiz;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuestionResultDetailDto {
    private int questionId;
    private String questionText;
    private String yourAnswer;
    private String correctAnswer;
    private String status;
}
