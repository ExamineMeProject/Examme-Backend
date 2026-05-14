package com.examme.examme.service;

import com.examme.examme.dto.response.exam.ExamPackageDetailDto;
import com.examme.examme.dto.response.exam.ExamPackageSummaryDto;
import com.examme.examme.dto.response.quiz.QuizQuestionResponseDto;
import com.examme.examme.dto.response.quiz.QuizQuestionWithAnswerDto;
import com.examme.examme.entity.ExamPackage;
import com.examme.examme.entity.QuizQuestion;
import com.examme.examme.entity.StudyGroup;
import com.examme.examme.entity.User;
import com.examme.examme.entity.enums.Difficulty;
import com.examme.examme.entity.enums.UserRole;
import com.examme.examme.exception.BadRequestException;
import com.examme.examme.exception.ForbiddenException;
import com.examme.examme.exception.NotFoundException;
import com.examme.examme.repository.ExamPackageRepository;
import com.examme.examme.repository.StudyGroupRepository;
import com.examme.examme.repository.UserRepository;
import com.examme.examme.security.SecurityUtils;
import com.examme.examme.util.FileProcessingUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ExamPackageService {

    private static final int MAX_TEXT_CHARS = 120_000;

    private final ExamPackageRepository examPackageRepository;
    private final StudyGroupRepository studyGroupRepository;
    private final UserRepository userRepository;
    private final FileProcessingUtil fileProcessingUtil;
    private final GeminiQuestionGeneratorService geminiQuestionGeneratorService;

    @Transactional
    public ExamPackageDetailDto create(Long groupId, MultipartFile file, int questionCount, Difficulty difficulty, String description)
            throws IOException {
        if (questionCount <= 0) {
            throw new BadRequestException("questionCount must be positive");
        }
        User teacher = userRepository.findByEmail(SecurityUtils.requireCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (teacher.getRole() != UserRole.TEACHER) {
            throw new ForbiddenException("Teacher access required");
        }
        StudyGroup group = studyGroupRepository.findById(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        if (!group.getTeacher().getId().equals(teacher.getId())) {
            throw new ForbiddenException("Bu qrup sizə aid deyil");
        }

        String extracted = fileProcessingUtil.extractTextFromFile(file);
        if (extracted.length() > MAX_TEXT_CHARS) {
            extracted = extracted.substring(0, MAX_TEXT_CHARS);
        }

        List<QuizQuestionResponseDto> generated = geminiQuestionGeneratorService.generateFromLectureText(
                extracted, questionCount, difficulty, description == null ? "" : description);

        String title = deriveTitle(file.getOriginalFilename());
        ExamPackage pkg = ExamPackage.builder()
                .title(title)
                .description(description)
                .group(group)
                .teacher(teacher)
                .difficulty(difficulty)
                .totalQuestions(generated.size())
                .build();

        List<QuizQuestion> questions = new ArrayList<>();
        for (QuizQuestionResponseDto dto : generated) {
            Map<String, String> opts = dto.getOptions();
            if (opts == null || !opts.containsKey("A") || !opts.containsKey("B") || !opts.containsKey("C") || !opts.containsKey("D")) {
                throw new BadRequestException("AI cavabında variantlar natamamdır");
            }
            String correct = dto.getCorrectAnswer() == null ? "" : dto.getCorrectAnswer().trim().toUpperCase();
            if (!List.of("A", "B", "C", "D").contains(correct)) {
                throw new BadRequestException("AI cavabında düzgün cavab hərfi yoxdur");
            }
            QuizQuestion q = QuizQuestion.builder()
                    .examPackage(pkg)
                    .questionId(dto.getQuestionId() != null ? dto.getQuestionId() : questions.size() + 1)
                    .questionText(dto.getQuestion() != null ? dto.getQuestion() : "")
                    .optionA(opts.get("A"))
                    .optionB(opts.get("B"))
                    .optionC(opts.get("C"))
                    .optionD(opts.get("D"))
                    .correctAnswer(correct)
                    .build();
            questions.add(q);
        }
        pkg.setQuestions(questions);
        pkg.setTotalQuestions(questions.size());
        ExamPackage saved = examPackageRepository.save(pkg);
        return toDetail(saved);
    }

    @Transactional(readOnly = true)
    public List<ExamPackageSummaryDto> listForGroup(Long groupId) {
        User user = userRepository.findByEmail(SecurityUtils.requireCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
        StudyGroup group = studyGroupRepository.findByIdWithStudents(groupId)
                .orElseThrow(() -> new NotFoundException("Group not found"));
        assertMemberOrTeacher(user, group);
        return examPackageRepository.findByGroupOrderByCreatedAtDesc(group).stream()
                .map(p -> ExamPackageSummaryDto.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .description(p.getDescription())
                        .groupId(group.getId())
                        .teacherId(p.getTeacher().getId())
                        .difficulty(p.getDifficulty())
                        .totalQuestions(p.getTotalQuestions())
                        .createdAt(p.getCreatedAt())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public ExamPackageDetailDto getDetail(Long id) {
        User user = userRepository.findByEmail(SecurityUtils.requireCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
        ExamPackage pkg = examPackageRepository.findByIdWithQuestions(id)
                .orElseThrow(() -> new NotFoundException("Exam package not found"));
        assertMemberOrTeacher(user, studyGroupRepository.findByIdWithStudents(pkg.getGroup().getId())
                .orElseThrow(() -> new NotFoundException("Group not found")));
        return toDetail(pkg);
    }

    @Transactional
    public void delete(Long id) {
        User user = userRepository.findByEmail(SecurityUtils.requireCurrentUserEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
        ExamPackage pkg = examPackageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Exam package not found"));
        if (!pkg.getTeacher().getId().equals(user.getId())) {
            throw new ForbiddenException("Yalnız öz paketlərinizi silə bilərsiniz");
        }
        examPackageRepository.delete(pkg);
    }

    private void assertMemberOrTeacher(User user, StudyGroup group) {
        boolean teacher = group.getTeacher().getId().equals(user.getId());
        boolean student = user.getRole() == UserRole.STUDENT
                && group.getStudents().stream().anyMatch(s -> s.getId().equals(user.getId()));
        if (!teacher && !student) {
            throw new ForbiddenException("Bu qrupun imtahan paketlərinə çıxışınız yoxdur");
        }
    }

    private String deriveTitle(String original) {
        if (original == null || original.isBlank()) {
            return "exam-package";
        }
        return original.replaceAll("(?i)\\.(pdf|docx|xlsx|txt)$", "");
    }

    private ExamPackageDetailDto toDetail(ExamPackage pkg) {
        List<QuizQuestionWithAnswerDto> qs = pkg.getQuestions().stream()
                .sorted((a, b) -> Integer.compare(a.getQuestionId(), b.getQuestionId()))
                .map(q -> QuizQuestionWithAnswerDto.builder()
                        .id(q.getId())
                        .questionId(q.getQuestionId())
                        .questionText(q.getQuestionText())
                        .optionA(q.getOptionA())
                        .optionB(q.getOptionB())
                        .optionC(q.getOptionC())
                        .optionD(q.getOptionD())
                        .correctAnswer(q.getCorrectAnswer())
                        .build())
                .toList();
        return ExamPackageDetailDto.builder()
                .id(pkg.getId())
                .title(pkg.getTitle())
                .description(pkg.getDescription())
                .groupId(pkg.getGroup().getId())
                .teacherId(pkg.getTeacher().getId())
                .difficulty(pkg.getDifficulty())
                .totalQuestions(pkg.getTotalQuestions())
                .createdAt(pkg.getCreatedAt())
                .questions(qs)
                .build();
    }
}
