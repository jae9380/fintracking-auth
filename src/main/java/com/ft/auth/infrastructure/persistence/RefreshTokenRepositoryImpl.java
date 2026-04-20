package com.ft.auth.infrastructure.persistence;

import com.ft.auth.application.port.RefreshTokenRepository;
import com.ft.auth.domain.RefreshToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepositoryImpl implements RefreshTokenRepository {

    private final JpaRefreshTokenRepository jpaRefreshTokenRepository;

    @Override
    public RefreshToken save(RefreshToken refreshToken) {
        return jpaRefreshTokenRepository.save(refreshToken);
    }

    @Override
    public Optional<RefreshToken> findByUserId(Long userId) {
        return jpaRefreshTokenRepository.findByUserId(userId);
    }

    @Override
    public Optional<RefreshToken> findByToken(String token) {
        return jpaRefreshTokenRepository.findByToken(token);
    }

    @Override
    public void deleteByUserId(Long userId) {
        jpaRefreshTokenRepository.deleteByUserId(userId);
    }
}
