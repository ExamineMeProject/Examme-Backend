package com.examme.examme.service;

import com.examme.examme.dto.projection.TeacherExamResultRowDto;
import com.examme.examme.dto.response.leaderboard.LeaderboardEntryDto;
import com.examme.examme.dto.response.quiz.MyResultHistoryDto;
import com.examme.examme.dto.response.quiz.QuestionResultDetailDto;
import com.examme.examme.entity.*;
import com.examme.examme.entity.enums.QuizSessionStatus;
import com.examme.examme.entity.enums.UserRole;
import com.examme.examme.exception.BadRequestException;
import com.examme.examme.exception.ForbiddenException;
import com.examme.examme.exception.NotFoundException;
import com.examme.examme.repository.*;
import com.examme.examme.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResultsService {

    private final UserRepository userRepository;
    private final ExamPackageRepository examPackageRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final QuizAnswerRepository quizAnswerRepository;

    @Transactional(readOnly = true)
    public List<TeacherExamResultRowDto> teacherGroupExamResults(Long groupId, Long examPackageId) {
        User user = userRepository.findByEmail(SecurityUtils.requireCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getRole() != UserRole.TEACHER) {
            throw new ForbiddenException("Only teachers can see the full results list");
        }
        StudyGroup group = studyGroupRepository.findByIdWithStudents(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        if (!group.getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("This group does not belong to you");
        }
        ExamPackage pkg = examPackageRepository.findById(examPackageId)
                .orElseThrow(() -> new NotFoundException("Exam package not found"));
        if (!pkg.getGroup().getId().equals(groupId)) {
            throw new BadRequestException("This package does not belong to this group");
        }

        Set<Long> studentIds = group.getStudents().stream().map(User::getId).collect(Collectors.toSet());
        List<QuizSession> completed = quizSessionRepository.findByExamPackageAndStatus(pkg, QuizSessionStatus.COMPLETED)
                .stream()
                .filter(s -> studentIds.contains(s.getStudent().getId()))
                .toList();

        Map<Long, QuizSession> best = pickBestSessionPerStudent(completed);
        return best.values().stream()
                .sorted(Comparator.comparing((QuizSession s) -> scorePercent(s)).reversed())
                .map(s -> toTeacherRow(s))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<LeaderboardEntryDto> studentLeaderboard(Long groupId, Long examPackageId) {
        User user = userRepository.findByEmail(SecurityUtils.requireCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
        StudyGroup group = studyGroupRepository.findByIdWithStudents(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        assertGroupAccess(user, group);

        ExamPackage pkg = examPackageRepository.findById(examPackageId)
                .orElseThrow(() -> new NotFoundException("Exam package not found"));
        if (!pkg.getGroup().getId().equals(groupId)) {
            throw new BadRequestException("This package does not belong to this group");
        }

        Set<Long> studentIds = group.getStudents().stream().map(User::getId).collect(Collectors.toSet());
        List<QuizSession> completed = quizSessionRepository.findByExamPackageAndStatus(pkg, QuizSessionStatus.COMPLETED)
                .stream()
                .filter(s -> studentIds.contains(s.getStudent().getId()))
                .toList();

        Map<Long, QuizSession> best = pickBestSessionPerStudent(completed);
        List<Map.Entry<Long, QuizSession>> sorted = best.entrySet().stream()
                .sorted((a, b) -> Double.compare(scorePercent(b.getValue()), scorePercent(a.getValue())))
                .toList();

        List<LeaderboardEntryDto> out = new ArrayList<>();
        int rank = 1;
        for (Map.Entry<Long, QuizSession> e : sorted) {
            User st = e.getValue().getStudent();
            out.add(LeaderboardEntryDto.builder()
                    .rank(rank++)
                    .studentName(st.getFullName())
                    .score(scorePercent(e.getValue()))
                    .build());
        }
        return out;
    }

    @Transactional(readOnly = true)
    public List<MyResultHistoryDto> myHistory() {
        User student = userRepository.findByEmail(SecurityUtils.requireCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (student.getRole() != UserRole.STUDENT) {
            throw new ForbiddenException("Only students can see their own history");
        }
        List<QuizSession> sessions = quizSessionRepository.findStudentHistory(student, QuizSessionStatus.COMPLETED);
        return sessions.stream().map(s -> {
            List<QuizAnswer> ans = quizAnswerRepository.findBySessionOrderByIdAsc(s);
            int total = ans.size();
            int correct = (int) ans.stream().filter(QuizAnswer::isCorrect).count();
            int skipped = (int) ans.stream().filter(a -> a.getSelectedAnswer() == null).count();
            int wrong = total - correct - skipped;
            double score = total == 0 ? 0 : Math.round(10000.0 * correct / total) / 100.0;
            return MyResultHistoryDto.builder()
                    .sessionId(s.getId())
                    .groupName(s.getExamPackage().getGroup().getName())
                    .examTitle(s.getExamPackage().getTitle())
                    .totalQuestions(total)
                    .correct(correct)
                    .wrong(wrong)
                    .skipped(skipped)
                    .score(score)
                    .finishedAt(s.getFinishedAt())
                    .details(ans.stream().map(this::mapToDetailDto).toList())
                    .build();
        }).toList();
    }

    private void assertGroupAccess(User user, StudyGroup group) {
        boolean teacher = user.getRole() == UserRole.TEACHER && group.getTeacher().getId().equals(user.getId());
        boolean student = user.getRole() == UserRole.STUDENT
                && group.getStudents().stream().anyMatch(s -> s.getId().equals(user.getId()));
        if (!teacher && !student) {
            throw new ForbiddenException("You do not have access to the results of this group");
        }
    }

    private Map<Long, QuizSession> pickBestSessionPerStudent(List<QuizSession> sessions) {
        Map<Long, QuizSession> best = new HashMap<>();
        for (QuizSession s : sessions) {
            Long sid = s.getStudent().getId();
            if (!best.containsKey(sid) || scorePercent(s) > scorePercent(best.get(sid))) {
                best.put(sid, s);
            }
        }
        return best;
    }

    private double scorePercent(QuizSession s) {
        List<QuizAnswer> ans = quizAnswerRepository.findBySessionOrderByIdAsc(s);
        if (ans.isEmpty()) {
            return 0;
        }
        long correct = ans.stream().filter(QuizAnswer::isCorrect).count();
        return Math.round(10000.0 * correct / ans.size()) / 100.0;
    }

    private TeacherExamResultRowDto toTeacherRow(QuizSession s) {
        List<QuizAnswer> ans = quizAnswerRepository.findBySessionOrderByIdAsc(s);
        int total = ans.size();
        int correct = (int) ans.stream().filter(QuizAnswer::isCorrect).count();
        int skipped = (int) ans.stream().filter(a -> a.getSelectedAnswer() == null).count();
        int wrong = total - correct - skipped;
        double score = total == 0 ? 0 : Math.round(10000.0 * correct / total) / 100.0;
        User st = s.getStudent();
        return TeacherExamResultRowDto.builder()
                .studentName(st.getFullName())
                .email(st.getEmail())
                .correct(correct)
                .wrong(wrong)
                .skipped(skipped)
                .score(score)
                .finishedAt(s.getFinishedAt())
                .details(ans.stream().map(this::mapToDetailDto).toList())
                .build();
    }

    private QuestionResultDetailDto mapToDetailDto(QuizAnswer a) {
        String status;
        if (a.getSelectedAnswer() == null) {
            status = "SKIPPED";
        } else if (a.isCorrect()) {
            status = "CORRECT";
        } else {
            status = "WRONG";
        }

        return QuestionResultDetailDto.builder()
                .questionId(a.getQuestion().getQuestionId())
                .questionText(a.getQuestion().getQuestionText())
                .yourAnswer(a.getSelectedAnswer())
                .correctAnswer(a.getQuestion().getCorrectAnswer())
                .status(status)
                .build();
    }
}
