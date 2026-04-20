package com.ft.auth.domain;

import com.ft.common.exception.CustomException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static com.ft.common.exception.ErrorCode.*;

@Entity
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String token;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private RefreshToken(Long userId, String token, LocalDateTime expiresAt) {
        this.userId = userId;
        this.token = token;
        this.expiresAt = expiresAt;
        this.createdAt = LocalDateTime.now();
    }

    public static RefreshToken create(Long userId, String token, LocalDateTime expiresAt) {
        if (token == null || token.isBlank()) {
            throw new CustomException(AUTH_REFRESH_TOKEN_REQUIRED);
        }
        if (expiresAt == null || expiresAt.isBefore(LocalDateTime.now())) {
            throw new CustomException(AUTH_REFRESH_TOKEN_INVALID);
        }
        return new RefreshToken(userId, token, expiresAt);
    }

    // 만료 여부 확인
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }

    // 토큰 유효성 검증
    public void validateToken(String token) {
        if (isExpired()) {
            throw new CustomException(AUTH_REFRESH_TOKEN_EXPIRED);
        }
        if (!this.token.equals(token)) {
            throw new CustomException(AUTH_REFRESH_TOKEN_INVALID);
        }
    }

    // 토큰 갱신 (Rotate)
    public void rotate(String newToken, LocalDateTime newExpiresAt) {
        if (newToken == null || newToken.isBlank()) {
            throw new CustomException(AUTH_NEW_REFRESH_TOKEN_REQUIRED);
        }
        this.token = newToken;
        this.expiresAt = newExpiresAt;
    }
}
