package com.examme.examme.service;

import com.examme.examme.dto.request.quiz.QuizStartRequestDto;
import com.examme.examme.dto.request.quiz.QuizSubmitAnswerDto;
import com.examme.examme.dto.request.quiz.QuizSubmitRequestDto;
import com.examme.examme.dto.response.quiz.QuestionResultDetailDto;
import com.examme.examme.dto.response.quiz.QuizQuestionPublicDto;
import com.examme.examme.dto.response.quiz.QuizResultDto;
import com.examme.examme.dto.response.quiz.QuizStartResponseDto;
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

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final UserRepository userRepository;
    private final ExamPackageRepository examPackageRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final QuizSessionRepository quizSessionRepository;
    private final QuizAnswerRepository quizAnswerRepository;

    @Transactional
    public QuizStartResponseDto start(QuizStartRequestDto req) {
        User student = userRepository.findByEmail(SecurityUtils.requireCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (student.getRole() != UserRole.STUDENT) {
            throw new ForbiddenException("Only students can start a test");
        }
        ExamPackage pkg = examPackageRepository.findById(req.getExamPackageId())
                .orElseThrow(() -> new NotFoundException("Exam package not found"));
        StudyGroup group = studyGroupRepository.findByIdWithStudents(pkg.getGroup().getId())
                .orElseThrow(() -> new NotFoundException("Group not found"));
        boolean member = group.getStudents().stream().anyMatch(s -> s.getId().equals(student.getId()));
        if (!member) {
            throw new ForbiddenException("You do not have access to this group's exams");
        }

        int range = req.getToQuestion() - req.getFromQuestion() + 1;
        if (range < req.getSelectedCount()) {
            throw new BadRequestException("Selected question count exceeds the available range");
        }

        List<QuizQuestion> pool = quizQuestionRepository.findByExamPackageAndQuestionIdBetweenOrderByQuestionIdAsc(
                pkg, req.getFromQuestion(), req.getToQuestion());
        if (pool.size() < req.getSelectedCount()) {
            throw new BadRequestException("Not enough questions available in this range");
        }

        List<QuizQuestion> picked = new ArrayList<>(pool);
        Collections.shuffle(picked);
        picked = new ArrayList<>(picked.subList(0, req.getSelectedCount()));

        QuizSession session = QuizSession.builder()
                .student(student)
                .examPackage(pkg)
                .fromQuestion(req.getFromQuestion())
                .toQuestion(req.getToQuestion())
                .selectedCount(req.getSelectedCount())
                .status(QuizSessionStatus.IN_PROGRESS)
                .build();
        session.getSelectedQuestions().addAll(picked);
        session = quizSessionRepository.save(session);

        List<QuizQuestionPublicDto> publicQs = picked.stream().map(this::toPublic).toList();
        return QuizStartResponseDto.builder()
                .sessionId(session.getId())
                .questions(publicQs)
                .build();
    }

    @Transactional
    public QuizResultDto submit(Long sessionId, QuizSubmitRequestDto body) {
        User student = userRepository.findByEmail(SecurityUtils.requireCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
        QuizSession session = quizSessionRepository.findByIdWithSelectedQuestions(sessionId)
                .orElseThrow(() -> new NotFoundException("Session not found"));
        if (!session.getStudent().getId().equals(student.getId())) {
            throw new ForbiddenException("This session does not belong to you");
        }
        if (session.getStatus() != QuizSessionStatus.IN_PROGRESS) {
            throw new BadRequestException("Session is already completed");
        }

        Map<Integer, QuizSubmitAnswerDto> byQuestionNumber = new HashMap<>();
        if (body.getAnswers() != null) {
            for (QuizSubmitAnswerDto a : body.getAnswers()) {
                byQuestionNumber.putIfAbsent(a.getQuestionId(), a);
            }
        }

        List<QuestionResultDetailDto> details = new ArrayList<>();
        int correct = 0;
        int wrong = 0;
        int skipped = 0;
        int total = session.getSelectedQuestions().size();

        List<QuizAnswer> toSave = new ArrayList<>();
        for (QuizQuestion q : session.getSelectedQuestions()) {
            QuizSubmitAnswerDto incoming = byQuestionNumber.get(q.getQuestionId());
            String selected = normalizeAnswer(incoming != null ? incoming.getSelectedAnswer() : null);
            boolean isCorrect = selected != null && selected.equalsIgnoreCase(q.getCorrectAnswer());
            if (selected == null) {
                skipped++;
            } else if (isCorrect) {
                correct++;
            } else {
                wrong++;
            }
            String status;
            if (selected == null) {
                status = "SKIPPED";
            } else if (isCorrect) {
                status = "CORRECT";
            } else {
                status = "WRONG";
            }
            details.add(QuestionResultDetailDto.builder()
                    .questionId(q.getQuestionId())
                    .questionText(q.getQuestionText())
                    .yourAnswer(selected)
                    .correctAnswer(q.getCorrectAnswer())
                    .status(status)
                    .build());

            toSave.add(QuizAnswer.builder()
                    .session(session)
                    .question(q)
                    .selectedAnswer(selected)
                    .correct(isCorrect)
                    .build());
        }

        quizAnswerRepository.saveAll(toSave);
        session.setStatus(QuizSessionStatus.COMPLETED);
        session.setFinishedAt(LocalDateTime.now());
        quizSessionRepository.save(session);

        double score = total == 0 ? 0.0 : Math.round(10000.0 * correct / total) / 100.0;
        return QuizResultDto.builder()
                .totalQuestions(total)
                .correct(correct)
                .wrong(wrong)
                .skipped(skipped)
                .score(score)
                .details(details)
                .build();
    }

    private String normalizeAnswer(String raw) {
        if (raw == null) {
            return null;
        }
        String t = raw.trim();
        if (t.isEmpty()) {
            return null;
        }
        return t.substring(0, 1).toUpperCase(Locale.ROOT);
    }

    private QuizQuestionPublicDto toPublic(QuizQuestion q) {
        return QuizQuestionPublicDto.builder()
                .id(q.getId())
                .questionId(q.getQuestionId())
                .questionText(q.getQuestionText())
                .optionA(q.getOptionA())
                .optionB(q.getOptionB())
                .optionC(q.getOptionC())
                .optionD(q.getOptionD())
                .correctAnswer(q.getCorrectAnswer())
                .build();
    }
}
