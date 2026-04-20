package com.ft.auth.infrastructure.oauth2.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.ft.auth.application.dto.KakaoUserInfo;
import com.ft.common.exception.CustomException;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.ft.common.exception.ErrorCode.AUTH_OAUTH2_EMAIL_MISSING;

/**
 * 카카오 유저 정보 API 응답 DTO
 * GET https://kapi.kakao.com/v2/user/me
 *
 * 카카오 응답 구조:
 * {
 *   "id": 12345,
 *   "kakao_account": {
 *     "email": "user@kakao.com",
 *     "is_email_verified": true
 *   }
 * }
 */
@Getter
@NoArgsConstructor
public class KakaoUserResponse {

    private Long id;

    @JsonProperty("kakao_account")
    private KakaoAccount kakaoAccount;

    /**
     * Application 레이어가 사용하는 KakaoUserInfo로 변환
     * 이메일이 없으면 예외 (카카오 계정에 이메일 미동의 상태)
     */
    public KakaoUserInfo toKakaoUserInfo() {
        if (kakaoAccount == null || kakaoAccount.getEmail() == null || kakaoAccount.getEmail().isBlank()) {
            throw new CustomException(AUTH_OAUTH2_EMAIL_MISSING);
        }
        return new KakaoUserInfo(kakaoAccount.getEmail(), String.valueOf(id));
    }

    @Getter
    @NoArgsConstructor
    public static class KakaoAccount {
        private String email;

        @JsonProperty("is_email_verified")
        private Boolean isEmailVerified;
    }
}
