package com.ft.auth.application.port;

import java.time.LocalDateTime;

public interface TokenProvider {
    String createAccessToken(Long userId);
    String createRefreshToken(Long userId);
    Long getUserId(String token);
    boolean validate(String token);
    LocalDateTime getRefreshTokenExpiry();
}
