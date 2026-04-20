package com.ft.auth.presentation.dto;

import com.ft.auth.application.dto.LoginCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
        @NotBlank @Email
        String email,

        @NotBlank
        String password
) {
    public LoginCommand toCommand() {
        return new LoginCommand(email, password);
    }
}
