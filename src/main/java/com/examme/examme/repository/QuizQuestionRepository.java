package com.examme.examme.repository;

import com.examme.examme.entity.ExamPackage;
import com.examme.examme.entity.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {
    List<QuizQuestion> findByExamPackageAndQuestionIdBetweenOrderByQuestionIdAsc(
            ExamPackage examPackage, int fromInclusive, int toInclusive);
}
