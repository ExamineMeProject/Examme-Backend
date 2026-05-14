package com.examme.examme.service;

import com.examme.examme.entity.GroupInvitation;
import com.examme.examme.entity.StudyGroup;
import com.examme.examme.entity.User;
import com.examme.examme.entity.enums.InvitationStatus;
import com.examme.examme.exception.BadRequestException;
import com.examme.examme.exception.ForbiddenException;
import com.examme.examme.exception.NotFoundException;
import com.examme.examme.repository.GroupInvitationRepository;
import com.examme.examme.repository.StudyGroupRepository;
import com.examme.examme.repository.UserRepository;
import com.examme.examme.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentInvitationService {

    private final GroupInvitationRepository invitationRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;

    @Transactional
    public void accept(Long invitationId) {
        User student = userRepository.findByEmail(SecurityUtils.requireCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
        GroupInvitation invitation = invitationRepository.findFetchedById(invitationId)
                .orElseThrow(() -> new NotFoundException("Dəvət tapılmadı"));
        if (!invitation.getStudent().getId().equals(student.getId())) {
            throw new ForbiddenException("Bu dəvət sizə aid deyil");
        }
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Dəvət artıq emal edilib");
        }
        StudyGroup group = studyGroupRepository.findByIdWithStudents(invitation.getGroup().getId())
                .orElseThrow(() -> new NotFoundException("Group not found"));
        invitation.setStatus(InvitationStatus.ACCEPTED);
        invitationRepository.save(invitation);
        group.getStudents().add(student);
        studyGroupRepository.save(group);
    }

    @Transactional
    public void reject(Long invitationId) {
        User student = userRepository.findByEmail(SecurityUtils.requireCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
        GroupInvitation invitation = invitationRepository.findFetchedById(invitationId)
                .orElseThrow(() -> new NotFoundException("Dəvət tapılmadı"));
        if (!invitation.getStudent().getId().equals(student.getId())) {
            throw new ForbiddenException("Bu dəvət sizə aid deyil");
        }
        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new BadRequestException("Dəvət artıq emal edilib");
        }
        invitation.setStatus(InvitationStatus.REJECTED);
        invitationRepository.save(invitation);
    }
}
