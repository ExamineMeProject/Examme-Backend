package com.examme.examme.repository;

import com.examme.examme.entity.QuizAnswer;
import com.examme.examme.entity.QuizSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuizAnswerRepository extends JpaRepository<QuizAnswer, Long> {
    void deleteBySession(QuizSession session);

    java.util.List<QuizAnswer> findBySessionOrderByIdAsc(QuizSession session);
}
