package com.ft.auth.application.dto;

/**
 * OAuth2 로그인 커맨드
 * 프론트엔드가 전달한 카카오 인가 코드를 담는다
 */
public record OAuth2LoginCommand(String code) {}
