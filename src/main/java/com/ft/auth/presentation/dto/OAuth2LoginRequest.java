package com.ft.auth.presentation.dto;

import com.ft.auth.application.dto.OAuth2LoginCommand;
import jakarta.validation.constraints.NotBlank;

public record OAuth2LoginRequest(

        @NotBlank(message = "인가 코드는 필수입니다.")
        String code

) {
    public OAuth2LoginCommand toCommand() {
        return new OAuth2LoginCommand(code);
    }
}
