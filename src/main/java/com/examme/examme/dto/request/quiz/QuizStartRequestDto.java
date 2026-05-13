package com.examme.examme.dto.request.quiz;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizStartRequestDto {
    @NotNull(message = "Exam package ID is required")
    private Long examPackageId;

    @Min(value = 0, message = "From question index must be at least 0")
    private int fromQuestion;

    @Min(value = 0, message = "To question index must be at least 0")
    private int toQuestion;

    @Min(value = 1, message = "Selected count must be at least 1")
    private int selectedCount;
}
