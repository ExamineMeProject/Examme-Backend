package com.examme.examme.dto.projection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminQuizResultRowDto {
    private Long sessionId;
    private String studentEmail;
    private String studentName;
    private Long examPackageId;
    private String examTitle;
    private int totalQuestions;
    private int correct;
    private int wrong;
    private int skipped;
    private double score;
    private LocalDateTime finishedAt;
}
