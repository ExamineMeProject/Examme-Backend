package com.examme.examme.service;

import com.examme.examme.dto.projection.AdminQuizResultRowDto;
import com.examme.examme.dto.projection.StudyGroupAdminDto;
import com.examme.examme.dto.response.exam.ExamPackageSummaryDto;
import com.examme.examme.dto.response.user.UserDto;
import com.examme.examme.entity.ExamPackage;
import com.examme.examme.entity.QuizAnswer;
import com.examme.examme.entity.QuizSession;
import com.examme.examme.entity.enums.QuizSessionStatus;
import com.examme.examme.repository.ExamPackageRepository;
import com.examme.examme.repository.QuizAnswerRepository;
import com.examme.examme.repository.QuizSessionRepository;
import com.examme.examme.repository.StudyGroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPanelService {

    private final UserService userService;
    private final StudyGroupRepository studyGroupRepository;
    private final ExamPackageRepository examPackageRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final QuizAnswerRepository quizAnswerRepository;

    @Transactional(readOnly = true)
    public List<UserDto> listUsers() {
        return userService.listAllUsers();
    }

    @Transactional(readOnly = true)
    public List<StudyGroupAdminDto> listGroups() {
        return studyGroupRepository.findAll().stream()
                .map(g -> StudyGroupAdminDto.builder()
                        .id(g.getId())
                        .name(g.getName())
                        .teacherId(g.getTeacher().getId())
                        .teacherEmail(g.getTeacher().getEmail())
                        .createdAt(g.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional
    public void deleteGroup(Long id) {
        studyGroupRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<ExamPackageSummaryDto> listExamPackages() {
        return examPackageRepository.findAll().stream()
                .map(p -> ExamPackageSummaryDto.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .description(p.getDescription())
                        .groupId(p.getGroup().getId())
                        .teacherId(p.getTeacher().getId())
                        .difficulty(p.getDifficulty())
                        .totalQuestions(p.getTotalQuestions())
                        .createdAt(p.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional
    public void deleteExamPackage(Long id) {
        examPackageRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<AdminQuizResultRowDto> listAllResults() {
        List<QuizSession> sessions = quizSessionRepository.findAllCompletedWithDetails(QuizSessionStatus.COMPLETED);
        return sessions.stream().map(this::toAdminRow).toList();
    }

    private AdminQuizResultRowDto toAdminRow(QuizSession s) {
        List<QuizAnswer> ans = quizAnswerRepository.findBySessionOrderByIdAsc(s);
        int total = ans.size();
        int correct = (int) ans.stream().filter(QuizAnswer::isCorrect).count();
        int skipped = (int) ans.stream().filter(a -> a.getSelectedAnswer() == null).count();
        int wrong = total - correct - skipped;
        double score = total == 0 ? 0 : Math.round(10000.0 * correct / total) / 100.0;
        ExamPackage ep = s.getExamPackage();
        return AdminQuizResultRowDto.builder()
                .sessionId(s.getId())
                .studentEmail(s.getStudent().getEmail())
                .studentName(s.getStudent().getFullName())
                .examPackageId(ep.getId())
                .examTitle(ep.getTitle())
                .totalQuestions(total)
                .correct(correct)
                .wrong(wrong)
                .skipped(skipped)
                .score(score)
                .finishedAt(s.getFinishedAt())
                .build();
    }
}
