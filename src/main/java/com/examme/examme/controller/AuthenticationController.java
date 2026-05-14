package com.examme.examme.controller;

import com.examme.examme.dto.request.auth.LoginRequestDto;
import com.examme.examme.dto.request.auth.RefreshRequestDto;
import com.examme.examme.dto.request.auth.UserRegistrationDto;
import com.examme.examme.dto.response.auth.AuthTokens;
import com.examme.examme.dto.response.auth.LoginResponseDto;
import com.examme.examme.dto.response.user.UserDto;
import com.examme.examme.entity.RefreshToken;
import com.examme.examme.entity.User;
import com.examme.examme.entity.enums.UserRole;
import com.examme.examme.exception.UnauthorizedException;
import com.examme.examme.service.RefreshTokenService;
import com.examme.examme.service.UserService;
import com.examme.examme.util.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication")
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", maxAge = 3600)
@RequiredArgsConstructor
public class AuthenticationController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Operation(summary = "Register as student or teacher")
    @PostMapping("/register")
    public ResponseEntity<LoginResponseDto> register(@Valid @RequestBody UserRegistrationDto registrationDto) {
        User user = userService.registerUser(
                registrationDto.getEmail(),
                registrationDto.getPassword(),
                registrationDto.getFullName(),
                UserRole.valueOf(registrationDto.getRole()));

        String access = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        String refresh = refreshTokenService.createRefreshToken(user);

        UserDto userDto = userService.getUserByEmail(user.getEmail());
        LoginResponseDto response = LoginResponseDto.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .user(userDto)
                .message("User registered successfully")
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Login and receive JWT")
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(@Valid @RequestBody LoginRequestDto loginRequest) {
        AuthTokens tokens = userService.login(loginRequest);
        UserDto user = userService.getUserByEmail(loginRequest.getEmail());
        LoginResponseDto response = LoginResponseDto.builder()
                .accessToken(tokens.getAccessToken())
                .refreshToken(tokens.getRefreshToken())
                .user(user)
                .message("Login successful")
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Refresh access token using refresh token JWT")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto> refresh(@RequestBody RefreshRequestDto request) {
        String refreshToken = request.getRefreshToken();
        if (refreshToken == null || !jwtTokenProvider.validateToken(refreshToken)
                || !jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        java.util.Optional<RefreshToken> stored = refreshTokenService.findByToken(refreshToken);
        if (stored.isEmpty()) {
            throw new UnauthorizedException("Refresh token not found or revoked");
        }
        if (refreshTokenService.isExpired(stored.get())) {
            throw new UnauthorizedException("Refresh token expired");
        }

        String email = jwtTokenProvider.getEmailFromToken(refreshToken);
        String role = jwtTokenProvider.getRoleFromToken(refreshToken);

        // generate new access token
        String access = jwtTokenProvider.generateToken(email, role);

        // rotate refresh token: delete old and create new
        User user = userService.requireUserByEmail(email);
        refreshTokenService.deleteByUser(user);
        String newRefresh = refreshTokenService.createRefreshToken(user);

        UserDto userDto = userService.getUserByEmail(email);
        LoginResponseDto response = LoginResponseDto.builder()
                .accessToken(access)
                .refreshToken(newRefresh)
                .user(userDto)
                .message("Token refreshed")
                .build();
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get current authenticated user profile", security = {
            @SecurityRequirement(name = "bearerAuth") })
    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            throw new UnauthorizedException("User not authenticated");
        }
        String email = authentication.getName();
        UserDto userDto = userService.getUserByEmail(email);
        return ResponseEntity.ok(userDto);
    }
}
