package com.examme.examme.service;

import com.examme.examme.dto.request.auth.LoginRequestDto;
import com.examme.examme.dto.request.user.UserUpdateDto;
import com.examme.examme.dto.response.auth.AuthTokens;
import com.examme.examme.dto.response.user.UserDto;
import com.examme.examme.entity.User;
import com.examme.examme.entity.enums.UserRole;
import com.examme.examme.exception.BadRequestException;
import com.examme.examme.exception.ConflictException;
import com.examme.examme.exception.NotFoundException;
import com.examme.examme.exception.UnauthorizedException;
import com.examme.examme.repository.UserRepository;
import com.examme.examme.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final com.examme.examme.repository.GroupInvitationRepository groupInvitationRepository;
    private final com.examme.examme.repository.StudyGroupRepository studyGroupRepository;
    private final com.examme.examme.repository.NotificationRepository notificationRepository;
    private final com.examme.examme.repository.QuizSessionRepository quizSessionRepository;

    @Transactional
    public User registerUser(String email, String password, String fullName, UserRole role) {
        if (role != UserRole.STUDENT && role != UserRole.TEACHER) {
            throw new BadRequestException("Yalnız STUDENT və ya TEACHER qeydiyyatı mümkündür");
        }
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ConflictException("User with this email already exists");
        }
        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .fullName(fullName)
                .role(role)
                .build();
        return userRepository.save(user);
    }

    public AuthTokens login(LoginRequestDto loginRequest) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        String access = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        String refresh = refreshTokenService.createRefreshToken(user);
        return new AuthTokens(access, refresh);
    }

    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return toDto(user);
    }

    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::toDto)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User requireUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public User requireUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<UserDto> listAllUsers() {
        return userRepository.findAll().stream().map(this::toDto).toList();
    }

    @Transactional
    public UserDto updateUser(Long id, UserUpdateDto dto) {
        User user = requireUserById(id);
        if (dto.getFullName() != null && !dto.getFullName().isBlank()) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            userRepository.findByEmail(dto.getEmail()).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new ConflictException("Email already in use");
                }
            });
            user.setEmail(dto.getEmail());
        }
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        return toDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("User not found");
        }
        User user = requireUserById(id);
        // remove group invitations
        try {
            groupInvitationRepository.deleteAllByStudent(user);
        } catch (Exception ignored) {}

        // remove notifications
        try {
            notificationRepository.findByUserOrderByCreatedAtDesc(user).forEach(n -> notificationRepository.delete(n));
        } catch (Exception ignored) {}

        // remove user from study groups
        try {
            studyGroupRepository.findByStudent(user).forEach(g -> {
                g.getStudents().remove(user);
                studyGroupRepository.save(g);
            });
        } catch (Exception ignored) {}

        // delete quiz sessions for student
        try {
            quizSessionRepository.findByStudent(user).forEach(s -> quizSessionRepository.delete(s));
        } catch (Exception ignored) {}

        // delete refresh tokens
        try {
            refreshTokenService.deleteByUser(user);
        } catch (Exception ignored) {}

        userRepository.deleteById(id);
    }

    @Transactional
    public UserDto updateUserRole(Long id, UserRole newRole) {
        User user = requireUserById(id);
        user.setRole(newRole);
        return toDto(userRepository.save(user));
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
