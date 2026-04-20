package com.ft.auth.application.handler;

import com.ft.auth.application.dto.LoginCommand;
import com.ft.auth.application.dto.LoginResult;
import com.ft.auth.application.port.RefreshTokenRepository;
import com.ft.auth.application.port.TokenProvider;
import com.ft.auth.application.port.UserRepository;
import com.ft.auth.domain.RefreshToken;
import com.ft.auth.domain.User;

public abstract class AbstractAuthHandler {

    protected final UserRepository userRepository;
    protected final RefreshTokenRepository refreshTokenRepository;
    protected final TokenProvider tokenProvider;

    protected AbstractAuthHandler(UserRepository userRepository,
                                  RefreshTokenRepository refreshTokenRepository,
                                  TokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.tokenProvider = tokenProvider;
    }

    // Template Method — 고정 흐름
    public final LoginResult login(LoginCommand command) {
        User user = loadUser(command);
        verifyCredentials(user, command);
        return issueTokens(user);
    }

    protected abstract User loadUser(LoginCommand command);

    protected abstract void verifyCredentials(User user, LoginCommand command);

    // 공통 토큰 발급 로직 — 서브클래스(KakaoAuthHandler 등)에서도 재사용 가능하도록 protected
    protected LoginResult issueTokens(User user) {
        String accessToken = tokenProvider.createAccessToken(user.getId());
        String rawRefreshToken = tokenProvider.createRefreshToken(user.getId());

        RefreshToken refreshToken = RefreshToken.create(
                user.getId(),
                rawRefreshToken,
                tokenProvider.getRefreshTokenExpiry()
        );

        // 기존 토큰 삭제 후 새 토큰 저장 (재로그인 시 교체)
        refreshTokenRepository.deleteByUserId(user.getId());
        refreshTokenRepository.save(refreshToken);

        return new LoginResult(accessToken, rawRefreshToken);
    }
}
