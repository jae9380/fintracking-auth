package com.ft.auth.application.port;

import com.ft.auth.application.dto.KakaoUserInfo;

/**
 * 카카오 OAuth2 외부 API 호출 포트
 * Application 레이어는 이 인터페이스에만 의존하고,
 * 실제 HTTP 통신은 Infrastructure의 KakaoOAuth2Client가 구현
 */
public interface KakaoOAuth2Port {

    /**
     * 인가 코드로 카카오 Access Token 발급
     * @param code 프론트엔드가 전달한 카카오 인가 코드
     * @return 카카오 Access Token
     */
    String getAccessToken(String code);

    /**
     * 카카오 Access Token으로 유저 정보 조회
     * @param accessToken 카카오 Access Token
     * @return 이메일 + kakaoId
     */
    KakaoUserInfo getUserInfo(String accessToken);
}
