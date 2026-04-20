package com.ft.auth.application.dto;

public record LoginResult(
        String accessToken,
        String refreshToken
) {}
