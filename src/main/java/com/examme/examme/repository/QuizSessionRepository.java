package com.examme.examme.repository;

import com.examme.examme.entity.ExamPackage;
import com.examme.examme.entity.QuizSession;
import com.examme.examme.entity.User;
import com.examme.examme.entity.enums.QuizSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface QuizSessionRepository extends JpaRepository<QuizSession, Long> {
    @Query("select s from QuizSession s join fetch s.student join fetch s.examPackage ep join fetch ep.group where s.id = :id")
    Optional<QuizSession> findByIdWithStudentAndPackage(@Param("id") Long id);

    @Query("select distinct s from QuizSession s join fetch s.selectedQuestions where s.id = :id")
    Optional<QuizSession> findByIdWithSelectedQuestions(@Param("id") Long id);

    List<QuizSession> findByExamPackageAndStatus(ExamPackage examPackage, QuizSessionStatus status);

    List<QuizSession> findByStudentAndStatusOrderByFinishedAtDesc(User student, QuizSessionStatus status);

    @Query("select s from QuizSession s join fetch s.examPackage ep join fetch ep.group where s.student = :student and s.status = :status order by s.finishedAt desc")
    List<QuizSession> findStudentHistory(@Param("student") User student, @Param("status") QuizSessionStatus status);

    List<QuizSession> findByStudent(User student);

    @Query("select s from QuizSession s join fetch s.student join fetch s.examPackage ep join fetch ep.group where s.status = :st order by s.finishedAt desc")
    List<QuizSession> findAllCompletedWithDetails(@Param("st") QuizSessionStatus status);
}
