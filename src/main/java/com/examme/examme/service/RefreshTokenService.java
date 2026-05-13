package com.examme.examme.service;

import com.examme.examme.entity.RefreshToken;
import com.examme.examme.entity.User;
import com.examme.examme.repository.RefreshTokenRepository;
import com.examme.examme.util.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public String createRefreshToken(User user) {
        String token = jwtTokenProvider.generateRefreshToken(user.getEmail(), user.getRole().name());
        Date exp = jwtTokenProvider.getExpiryDateFromToken(token);
        Instant expiry = exp != null ? exp.toInstant() : Instant.now().plusSeconds(60L * 60L * 24L * 30L);
        RefreshToken rt = RefreshToken.builder()
                .user(user)
                .token(token)
                .expiryDate(expiry)
                .build();
        refreshTokenRepository.save(rt);
        return token;
    }

    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public boolean isExpired(RefreshToken token) {
        return token.getExpiryDate().isBefore(Instant.now());
    }
}
