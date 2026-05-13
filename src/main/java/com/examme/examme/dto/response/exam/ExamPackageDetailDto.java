package com.examme.examme.dto.response.exam;

import com.examme.examme.dto.response.quiz.QuizQuestionWithAnswerDto;
import com.examme.examme.entity.enums.Difficulty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamPackageDetailDto {
    private Long id;
    private String title;
    private String description;
    private Long groupId;
    private Long teacherId;
    private Difficulty difficulty;
    private int totalQuestions;
    private LocalDateTime createdAt;
    private List<QuizQuestionWithAnswerDto> questions;
}
