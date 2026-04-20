package com.ft.auth.application.dto;

/**
 * 카카오 API로부터 파싱된 유저 핵심 정보
 * Infrastructure 응답 DTO(KakaoUserResponse)와 분리하여 Application 레이어에서 사용
 */
public record KakaoUserInfo(
        String email,
        String kakaoId
) {}
