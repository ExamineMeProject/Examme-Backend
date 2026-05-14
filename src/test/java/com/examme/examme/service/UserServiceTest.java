package com.examme.examme.service;

import com.examme.examme.dto.request.auth.LoginRequestDto;
import com.examme.examme.dto.response.auth.AuthTokens;
import com.examme.examme.entity.User;
import com.examme.examme.entity.enums.UserRole;
import com.examme.examme.exception.ConflictException;
import com.examme.examme.exception.NotFoundException;
import com.examme.examme.exception.UnauthorizedException;
import com.examme.examme.repository.UserRepository;
import com.examme.examme.util.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenProvider jwtTokenProvider;
    @Mock
    private RefreshTokenService refreshTokenService;
    @Mock
    private com.examme.examme.repository.GroupInvitationRepository groupInvitationRepository;
    @Mock
    private com.examme.examme.repository.StudyGroupRepository studyGroupRepository;
    @Mock
    private com.examme.examme.repository.NotificationRepository notificationRepository;
    @Mock
    private com.examme.examme.repository.QuizSessionRepository quizSessionRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("encodedPassword")
                .fullName("Test User")
                .role(UserRole.STUDENT)
                .build();
    }

    @Test
    void registerUser_Success() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User registered = userService.registerUser("test@example.com", "password", "Test User", UserRole.STUDENT);

        assertNotNull(registered);
        assertEquals("test@example.com", registered.getEmail());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerUser_AlreadyExists_ThrowsConflict() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));

        assertThrows(ConflictException.class, () -> 
            userService.registerUser("test@example.com", "password", "Test User", UserRole.STUDENT));
    }

    @Test
    void login_Success() {
        LoginRequestDto loginRequest = new LoginRequestDto("test@example.com", "password");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateToken(anyString(), anyString())).thenReturn("accessToken");
        when(refreshTokenService.createRefreshToken(any(User.class))).thenReturn("refreshToken");

        AuthTokens tokens = userService.login(loginRequest);

        assertNotNull(tokens);
        assertEquals("accessToken", tokens.getAccessToken());
        assertEquals("refreshToken", tokens.getRefreshToken());
    }

    @Test
    void login_InvalidPassword_ThrowsUnauthorized() {
        LoginRequestDto loginRequest = new LoginRequestDto("test@example.com", "wrong");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong", "encodedPassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> userService.login(loginRequest));
    }

    @Test
    void getUserByEmail_NotFound_ThrowsNotFound() {
        when(userRepository.findByEmail("none@example.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getUserByEmail("none@example.com"));
    }
}
