package com.ft.auth.presentation.dto;

import com.ft.auth.application.dto.SignupCommand;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignupRequest(
        @NotBlank @Email
        String email,

        @NotBlank @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
        String password
) {
    public SignupCommand toCommand() {
        return new SignupCommand(email, password);
    }
}
