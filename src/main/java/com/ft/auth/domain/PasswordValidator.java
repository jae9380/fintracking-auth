package com.ft.auth.domain;

// NOTE: 임시 작성
// 도메인이 Spring Security에 직접 의존하지 않도록 인터페이스로 분리
// 구현체는 Infrastructure 레이어에서 BCryptPasswordEncoder로 제공
@FunctionalInterface
public interface PasswordValidator {
    boolean matches(String rawPassword, String encodedPassword);
}
