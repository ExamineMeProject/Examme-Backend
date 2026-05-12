package com.examme.examme.repository;

import com.examme.examme.entity.ExamPackage;
import com.examme.examme.entity.StudyGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ExamPackageRepository extends JpaRepository<ExamPackage, Long> {
    List<ExamPackage> findByGroupOrderByCreatedAtDesc(StudyGroup group);

    @Query("select ep from ExamPackage ep join fetch ep.group join fetch ep.teacher left join fetch ep.questions where ep.id = :id")
    Optional<ExamPackage> findByIdWithQuestions(@Param("id") Long id);
}
