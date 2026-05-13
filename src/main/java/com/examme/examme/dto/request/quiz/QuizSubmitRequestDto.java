package com.examme.examme.dto.request.quiz;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizSubmitRequestDto {
    @NotEmpty(message = "Answers list cannot be empty")
    private List<QuizSubmitAnswerDto> answers;
}
