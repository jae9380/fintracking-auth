package com.ft.auth.presentation.dto;

import com.ft.auth.application.dto.LoginResult;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
    public static AuthResponse from(LoginResult result) {
        return new AuthResponse(result.accessToken(), result.refreshToken());
    }
}
