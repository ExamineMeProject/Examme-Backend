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
public class QuizStartResponseDto {
    private Long sessionId;
    private List<QuizQuestionPublicDto> questions;
}
