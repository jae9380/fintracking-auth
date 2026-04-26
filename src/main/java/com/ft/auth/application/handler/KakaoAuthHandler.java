package com.ft.auth.application.handler;

import com.ft.auth.application.UserRegisteredEventPublisher;
import com.ft.auth.application.dto.KakaoUserInfo;
import com.ft.auth.application.dto.LoginCommand;
import com.ft.auth.application.dto.LoginResult;
import com.ft.auth.application.dto.OAuth2LoginCommand;
import com.ft.auth.application.port.KakaoOAuth2Port;
import com.ft.auth.application.port.RefreshTokenRepository;
import com.ft.auth.application.port.TokenProvider;
import com.ft.auth.application.port.UserRepository;
import com.ft.auth.domain.OAuth2Provider;
import com.ft.auth.domain.User;
import com.ft.common.event.UserRegisteredEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 카카오 OAuth2 로그인 핸들러 (Template Method 패턴)
 *
 * AbstractAuthHandler의 login(LoginCommand) 템플릿은 email/password 기반이므로
 * OAuth2는 별도 진입점 loginWithOAuth2(OAuth2LoginCommand)를 제공한다.
 * 토큰 발급 공통 로직(issueTokens)은 부모 클래스의 protected 메서드를 재사용한다.
 *
 * 플로우:
 * 1. 카카오 인가 코드 → 카카오 Access Token 교환
 * 2. 카카오 Access Token → 유저 정보(email, kakaoId) 조회
 * 3. email로 유저 조회 — 없으면 신규 생성 (Find or Create)
 * 4. JWT 발급 후 반환
 */
@Component
public class KakaoAuthHandler extends AbstractAuthHandler {

    private final KakaoOAuth2Port kakaoOAuth2Port;
    private final UserRegisteredEventPublisher userRegisteredEventPublisher;

    public KakaoAuthHandler(UserRepository userRepository,
                            RefreshTokenRepository refreshTokenRepository,
                            TokenProvider tokenProvider,
                            KakaoOAuth2Port kakaoOAuth2Port,
                            UserRegisteredEventPublisher userRegisteredEventPublisher) {
        super(userRepository, refreshTokenRepository, tokenProvider);
        this.kakaoOAuth2Port = kakaoOAuth2Port;
        this.userRegisteredEventPublisher = userRegisteredEventPublisher;
    }

    /**
     * OAuth2 로그인 진입점
     * AbstractAuthHandler.login(LoginCommand)와 별도로 존재하며,
     * 공통 토큰 발급 로직(issueTokens)을 내부에서 재사용한다.
     */
    public LoginResult loginWithOAuth2(OAuth2LoginCommand command) {
        String kakaoAccessToken = kakaoOAuth2Port.getAccessToken(command.code());
        KakaoUserInfo userInfo = kakaoOAuth2Port.getUserInfo(kakaoAccessToken);

        User user = findOrCreateUser(userInfo);
        return issueTokens(user);
    }

    /**
     * email로 유저 조회, 없으면 OAuth2 유저로 신규 생성 (Find or Create 패턴)
     */
    private User findOrCreateUser(KakaoUserInfo userInfo) {
        return userRepository.findByEmail(userInfo.email())
                .orElseGet(() -> {
                    User newUser = User.createOAuth2User(userInfo.email(), OAuth2Provider.KAKAO);
                    User saved = userRepository.save(newUser);

                    userRegisteredEventPublisher.publish(new UserRegisteredEvent(
                            UUID.randomUUID().toString(),
                            saved.getId(),
                            saved.getEmail()
                    ));

                    return saved;
                });
    }

    /**
     * AbstractAuthHandler의 추상 메서드 구현
     * OAuth2 플로우에서는 사용되지 않으나, 템플릿 계약상 구현 필요
     * loginWithOAuth2()를 통해 진입하므로 이 경로는 실제로 호출되지 않는다.
     */
    @Override
    protected User loadUser(LoginCommand command) {
        throw new UnsupportedOperationException("KakaoAuthHandler는 loginWithOAuth2()를 사용하세요.");
    }

    /**
     * OAuth2 유저는 별도 자격 증명 검증이 불필요 (카카오가 대신 수행)
     */
    @Override
    protected void verifyCredentials(User user, LoginCommand command) {
        // no-op: 카카오 OAuth2 인증 성공 자체가 자격 증명
    }
}
