package com.examme.examme.dto.projection;

import com.examme.examme.dto.response.quiz.QuestionResultDetailDto;
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
public class TeacherExamResultRowDto {
    private String studentName;
    private String email;
    private int correct;
    private int wrong;
    private int skipped;
    private double score;
    private LocalDateTime finishedAt;
    private List<QuestionResultDetailDto> details;
}
