package com.examme.examme.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "quiz_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class QuizQuestion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exam_package_id", nullable = false)
    private ExamPackage examPackage;

    @Column(name = "question_number", nullable = false)
    private int questionId;

    @NotBlank(message = "Question text is required")
    @Size(max = 4000, message = "Question text is too long")
    @Column(name = "question_text", nullable = false, length = 4000)
    private String questionText;

    @NotBlank(message = "Option A is required")
    @Size(max = 2000, message = "Option A is too long")
    @Column(name = "option_a", nullable = false, length = 2000)
    private String optionA;

    @NotBlank(message = "Option B is required")
    @Size(max = 2000, message = "Option B is too long")
    @Column(name = "option_b", nullable = false, length = 2000)
    private String optionB;

    @NotBlank(message = "Option C is required")
    @Size(max = 2000, message = "Option C is too long")
    @Column(name = "option_c", nullable = false, length = 2000)
    private String optionC;

    @NotBlank(message = "Option D is required")
    @Size(max = 2000, message = "Option D is too long")
    @Column(name = "option_d", nullable = false, length = 2000)
    private String optionD;

    @NotBlank(message = "Correct answer is required")
    @Size(max = 1, message = "Correct answer must be a single letter")
    @Column(name = "correct_answer", nullable = false, length = 1)
    private String correctAnswer;
}
