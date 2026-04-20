package com.ft.auth.presentation;

import com.ft.auth.application.AuthService;
import com.ft.auth.application.dto.LoginResult;
import com.ft.auth.presentation.dto.AuthResponse;
import com.ft.auth.presentation.dto.LoginRequest;
import com.ft.auth.presentation.dto.OAuth2LoginRequest;
import com.ft.auth.presentation.dto.SignupRequest;
import com.ft.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "이메일 회원가입")
    @PostMapping("/signup")
    public ApiResponse<Long> signup(@Valid @RequestBody SignupRequest request) {
        Long userId = authService.signup(request.toCommand());
        return ApiResponse.created(userId);
    }

    @Operation(summary = "이메일 로그인")
    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResult result = authService.login(request.toCommand());
        return ApiResponse.success(AuthResponse.from(result));
    }

    @Operation(summary = "카카오 OAuth2 로그인")
    @PostMapping("/oauth2/login")
    public ApiResponse<AuthResponse> oauth2Login(@Valid @RequestBody OAuth2LoginRequest request) {
        LoginResult result = authService.oauth2Login(request.toCommand());
        return ApiResponse.success(AuthResponse.from(result));
    }

    @Operation(summary = "Access Token 재발급")
    @PostMapping("/reissue")
    public ApiResponse<String> reissue(
            @RequestHeader("Authorization") String bearerToken) {
        String refreshToken = bearerToken.replace("Bearer ", "");
        String newAccessToken = authService.reissue(refreshToken);
        return ApiResponse.success(newAccessToken);
    }

    @Operation(summary = "로그아웃")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal Long userId) {
        authService.logout(userId);
        return ApiResponse.noContent();
    }
}
