package com.examme.examme.repository;

import com.examme.examme.entity.GroupInvitation;
import com.examme.examme.entity.StudyGroup;
import com.examme.examme.entity.User;
import com.examme.examme.entity.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface GroupInvitationRepository extends JpaRepository<GroupInvitation, Long> {
    boolean existsByGroupAndStudentAndStatus(StudyGroup group, User student, InvitationStatus status);

    Optional<GroupInvitation> findByIdAndStudent(Long id, User student);

    @Query("SELECT i FROM GroupInvitation i JOIN FETCH i.group JOIN FETCH i.student WHERE i.id = :id")
    Optional<GroupInvitation> findFetchedById(@Param("id") Long id);

    void deleteAllByStudent(User student);
}
