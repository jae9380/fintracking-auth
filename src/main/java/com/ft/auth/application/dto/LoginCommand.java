package com.ft.auth.application.dto;

public record LoginCommand(
        String email,
        String rawPassword
) {}
