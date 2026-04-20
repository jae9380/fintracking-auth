package com.ft.auth.infrastructure.oauth2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 카카오 토큰 발급 API 응답 DTO
 * POST https://kauth.kakao.com/oauth/token
 */
@Getter
@NoArgsConstructor
public class KakaoTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private Long expiresIn;

    @JsonProperty("refresh_token_expires_in")
    private Long refreshTokenExpiresIn;
}
