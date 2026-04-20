package com.ft.auth.application.port;

import com.ft.auth.domain.RefreshToken;

import java.util.Optional;

public interface RefreshTokenRepository {
    RefreshToken save(RefreshToken refreshToken);
    Optional<RefreshToken> findByUserId(Long userId);
    Optional<RefreshToken> findByToken(String token);
    void deleteByUserId(Long userId);
}
