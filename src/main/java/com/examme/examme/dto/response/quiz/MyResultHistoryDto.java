package com.examme.examme.dto.response.quiz;

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
public class MyResultHistoryDto {
    private Long sessionId;
    private String groupName;
    private String examTitle;
    private int totalQuestions;
    private int correct;
    private int wrong;
    private int skipped;
    private double score;
    private LocalDateTime finishedAt;
    private List<QuestionResultDetailDto> details;
}
