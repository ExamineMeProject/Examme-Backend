package com.examme.examme.service;

import com.examme.examme.dto.request.group.GroupRequestDto;
import com.examme.examme.dto.response.group.GroupResponseDto;
import com.examme.examme.dto.response.user.StudentBriefDto;
import com.examme.examme.entity.GroupInvitation;
import com.examme.examme.entity.Notification;
import com.examme.examme.entity.StudyGroup;
import com.examme.examme.entity.User;
import com.examme.examme.entity.enums.InvitationStatus;
import com.examme.examme.entity.enums.UserRole;
import com.examme.examme.exception.BadRequestException;
import com.examme.examme.exception.ConflictException;
import com.examme.examme.exception.ForbiddenException;
import com.examme.examme.exception.NotFoundException;
import com.examme.examme.repository.GroupInvitationRepository;
import com.examme.examme.repository.NotificationRepository;
import com.examme.examme.repository.StudyGroupRepository;
import com.examme.examme.repository.UserRepository;
import com.examme.examme.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StudyGroupService {

    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final GroupInvitationRepository groupInvitationRepository;
    private final NotificationRepository notificationRepository;
    private final MailService mailService;

    @Transactional
    public GroupResponseDto create(GroupRequestDto dto) {
        User teacher = userRepository.findByEmail(SecurityUtils.requireCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (teacher.getRole() != UserRole.TEACHER) {
            throw new ForbiddenException("Teacher access required");
        }
        StudyGroup group = StudyGroup.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .teacher(teacher)
                .build();
        group = studyGroupRepository.save(group);
        return toSummaryDto(group);
    }

    @Transactional(readOnly = true)
    public List<GroupResponseDto> listMine() {
        User teacher = userRepository.findByEmail(SecurityUtils.requireCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return studyGroupRepository.findByTeacherOrderByCreatedAtDesc(teacher).stream()
                .map(this::toSummaryDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public GroupResponseDto getDetail(Long id) {
        StudyGroup group = studyGroupRepository.findByIdWithStudents(id)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        assertTeacherOwns(group);
        List<StudentBriefDto> students = group.getStudents().stream()
                .sorted(Comparator.comparing(User::getFullName))
                .map(u -> StudentBriefDto.builder().id(u.getId()).fullName(u.getFullName()).email(u.getEmail()).build())
                .toList();
        GroupResponseDto dto = toSummaryDto(group);
        dto.setStudents(students);
        return dto;
    }

    @Transactional
    public GroupResponseDto update(Long id, GroupRequestDto dto) {
        StudyGroup group = studyGroupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        assertTeacherOwns(group);
        group.setName(dto.getName());
        group.setDescription(dto.getDescription());
        return toSummaryDto(studyGroupRepository.save(group));
    }

    @Transactional
    public void delete(Long id) {
        StudyGroup group = studyGroupRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        assertTeacherOwns(group);
        studyGroupRepository.delete(group);
    }

    @Transactional
    public void inviteStudent(Long groupId, String email) {
        StudyGroup group = studyGroupRepository.findByIdWithStudents(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        assertTeacherOwns(group);
        User student = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Student with this email not found"));
        if (student.getRole() != UserRole.STUDENT) {
            throw new BadRequestException("This email does not belong to a student account");
        }
        if (group.getStudents().contains(student)) {
            throw new ConflictException("Student is already in the group");
        }
        if (groupInvitationRepository.existsByGroupAndStudentAndStatus(group, student, InvitationStatus.PENDING)) {
            throw new ConflictException("A pending invitation already exists for this student");
        }
        GroupInvitation invitation = GroupInvitation.builder()
                .group(group)
                .student(student)
                .status(InvitationStatus.PENDING)
                .build();
        invitation = groupInvitationRepository.save(invitation);

        String message = "You have been invited to join the group \"" + group.getName() + "\"";
        notificationRepository.save(Notification.builder()
                .user(student)
                .message(message)
                .invitationId(invitation.getId())
                .readFlag(false)
                .build());

        mailService.sendPlainText(
                student.getEmail(),
                "Group Invitation",
                message
        );
    }

    public StudyGroup requireGroupOwnedByCurrentTeacher(Long groupId) {
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        assertTeacherOwns(group);
        return group;
    }

    @Transactional(readOnly = true)
    public StudyGroup requireGroupForMember(Long groupId, User user) {
        StudyGroup group = studyGroupRepository.findByIdWithStudents(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        boolean teacher = group.getTeacher().getId().equals(user.getId());
        boolean student = group.getStudents().stream().anyMatch(s -> s.getId().equals(user.getId()));
        if (!teacher && !student) {
            throw new ForbiddenException("You do not have access to this group");
        }
        return group;
    }

    private void assertTeacherOwns(StudyGroup group) {
        User current = userRepository.findByEmail(SecurityUtils.requireCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!group.getTeacher().getId().equals(current.getId())) {
            throw new ForbiddenException("This group does not belong to you");
        }
    }

    private GroupResponseDto toSummaryDto(StudyGroup group) {
        List<StudentBriefDto> students = group.getStudents() == null ? List.of() : group.getStudents().stream()
                .map(u -> StudentBriefDto.builder().id(u.getId()).fullName(u.getFullName()).email(u.getEmail()).build())
                .toList();

        return GroupResponseDto.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .teacherId(group.getTeacher().getId())
                .studentCount(students.size())
                .createdAt(group.getCreatedAt())
                .students(students)
                .build();
    }
}
