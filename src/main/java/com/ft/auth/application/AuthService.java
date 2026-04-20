package com.ft.auth.application;

import com.ft.auth.application.dto.LoginCommand;
import com.ft.auth.application.dto.LoginResult;
import com.ft.auth.application.dto.OAuth2LoginCommand;
import com.ft.auth.application.dto.SignupCommand;
import com.ft.auth.application.handler.EmailAuthHandler;
import com.ft.auth.application.handler.KakaoAuthHandler;
import com.ft.auth.application.port.RefreshTokenRepository;
import com.ft.auth.application.port.TokenProvider;
import com.ft.auth.application.port.UserRepository;
import com.ft.auth.domain.RefreshToken;
import com.ft.auth.domain.User;
import com.ft.common.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.ft.common.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenProvider tokenProvider;
    private final EmailAuthHandler emailAuthHandler;
    private final KakaoAuthHandler kakaoAuthHandler;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    @Transactional
    public Long signup(SignupCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new CustomException(AUTH_EMAIL_EXISTS);
        }
        String encodedPassword = passwordEncoder.encode(command.rawPassword());
        User user = User.create(command.email(), encodedPassword);
        return userRepository.save(user).getId();
    }

    // 이메일 로그인
    @Transactional
    public LoginResult login(LoginCommand command) {
        return emailAuthHandler.login(command);
    }

    // 카카오 OAuth2 로그인 (Find or Create)
    @Transactional
    public LoginResult oauth2Login(OAuth2LoginCommand command) {
        return kakaoAuthHandler.loginWithOAuth2(command);
    }

    // Access Token 재발급
    @Transactional
    public String reissue(String rawRefreshToken) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(rawRefreshToken)
                .orElseThrow(() -> new CustomException(AUTH_INVALID_TOKEN));

        refreshToken.validateToken(rawRefreshToken);

        String newAccessToken = tokenProvider.createAccessToken(refreshToken.getUserId());

        // Refresh Token Rotation
        String newRawRefreshToken = tokenProvider.createRefreshToken(refreshToken.getUserId());
        refreshToken.rotate(newRawRefreshToken, tokenProvider.getRefreshTokenExpiry());
        refreshTokenRepository.save(refreshToken);

        return newAccessToken;
    }

    // 로그아웃
    @Transactional
    public void logout(Long userId) {
        refreshTokenRepository.deleteByUserId(userId);
    }
}
