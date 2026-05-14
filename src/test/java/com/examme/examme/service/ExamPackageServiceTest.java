package com.examme.examme.service;

import com.examme.examme.dto.response.quiz.QuizQuestionResponseDto;
import com.examme.examme.entity.ExamPackage;
import com.examme.examme.entity.StudyGroup;
import com.examme.examme.entity.User;
import com.examme.examme.entity.enums.Difficulty;
import com.examme.examme.entity.enums.UserRole;
import com.examme.examme.exception.NotFoundException;
import com.examme.examme.repository.ExamPackageRepository;
import com.examme.examme.repository.StudyGroupRepository;
import com.examme.examme.repository.UserRepository;
import com.examme.examme.security.SecurityUtils;
import com.examme.examme.util.FileProcessingUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExamPackageServiceTest {

    @Mock
    private ExamPackageRepository examPackageRepository;
    @Mock
    private StudyGroupRepository studyGroupRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private FileProcessingUtil fileProcessingUtil;
    @Mock
    private GeminiQuestionGeneratorService geminiQuestionGeneratorService;

    @InjectMocks
    private ExamPackageService examPackageService;

    private User teacher;
    private StudyGroup group;

    @BeforeEach
    void setUp() {
        teacher = User.builder().id(1L).email("teacher@example.com").role(UserRole.TEACHER).build();
        group = StudyGroup.builder().id(1L).teacher(teacher).build();
    }

    @Test
    void create_Success() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.pdf");
        
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::requireCurrentUserEmail).thenReturn("teacher@example.com");
            
            when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(teacher));
            when(studyGroupRepository.findById(1L)).thenReturn(Optional.of(group));
            when(fileProcessingUtil.extractTextFromFile(any())).thenReturn("extracted text");
            
            QuizQuestionResponseDto qDto = new QuizQuestionResponseDto();
            qDto.setQuestionId(1);
            qDto.setQuestion("Test?");
            qDto.setOptions(Map.of("A", "1", "B", "2", "C", "3", "D", "4"));
            qDto.setCorrectAnswer("A");
            
            when(geminiQuestionGeneratorService.generateFromLectureText(anyString(), anyInt(), any(), anyString()))
                    .thenReturn(List.of(qDto));
            
            when(examPackageRepository.save(any(ExamPackage.class))).thenAnswer(i -> {
                ExamPackage p = i.getArgument(0);
                p.setId(10L);
                return p;
            });

            var result = examPackageService.create(1L, file, 1, Difficulty.EASY, "Desc");

            assertNotNull(result);
            assertEquals(10L, result.getId());
            verify(examPackageRepository).save(any(ExamPackage.class));
        }
    }

    @Test
    void delete_Success() {
        ExamPackage pkg = ExamPackage.builder().id(10L).teacher(teacher).build();
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::requireCurrentUserEmail).thenReturn("teacher@example.com");
            when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(teacher));
            when(examPackageRepository.findById(10L)).thenReturn(Optional.of(pkg));

            examPackageService.delete(10L);

            verify(examPackageRepository).delete(pkg);
        }
    }

    @Test
    void getDetail_NotFound_ThrowsNotFound() {
        try (MockedStatic<SecurityUtils> mockedSecurity = mockStatic(SecurityUtils.class)) {
            mockedSecurity.when(SecurityUtils::requireCurrentUserEmail).thenReturn("teacher@example.com");
            when(userRepository.findByEmail("teacher@example.com")).thenReturn(Optional.of(teacher));
            when(examPackageRepository.findByIdWithQuestions(anyLong())).thenReturn(Optional.empty());

            assertThrows(NotFoundException.class, () -> examPackageService.getDetail(99L));
        }
    }
}
