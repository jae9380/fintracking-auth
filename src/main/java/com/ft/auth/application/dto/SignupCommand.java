package com.ft.auth.application.dto;

public record SignupCommand(
        String email,
        String rawPassword
) {}
