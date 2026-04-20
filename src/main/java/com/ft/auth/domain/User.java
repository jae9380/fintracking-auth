package com.ft.auth.domain;

import com.ft.common.entity.BaseEntity;
import com.ft.common.exception.CustomException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static com.ft.common.exception.ErrorCode.*;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    // OAuth2 유저는 비밀번호가 없으므로 nullable
    @Column(nullable = true)
    private String password;  // BCrypt 해시

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OAuth2Provider provider;

    // 이메일 로그인 생성자
    private User(String email, String password) {
        this.email = email;
        this.password = password;
        this.provider = OAuth2Provider.EMAIL;
    }

    // OAuth2 로그인 생성자
    private User(String email, OAuth2Provider provider) {
        this.email = email;
        this.password = null;
        this.provider = provider;
    }

    // 이메일/비밀번호 회원가입 (기존 메서드 유지)
    public static User create(String email, String encodedPassword) {
        validateEmail(email);
        return new User(email, encodedPassword);
    }

    // OAuth2 신규 유저 생성 (Find or Create 패턴에서 사용)
    public static User createOAuth2User(String email, OAuth2Provider provider) {
        validateEmail(email);
        if (provider == null || provider == OAuth2Provider.EMAIL) {
            throw new CustomException(AUTH_OAUTH2_FAILED);
        }
        return new User(email, provider);
    }

    // 비밀번호 검증 — PasswordValidator로 Spring 의존성 분리
    public void validatePassword(String rawPassword, PasswordValidator validator) {
        if (!validator.matches(rawPassword, this.password)) {
            throw new CustomException(AUTH_INVALID_PASSWORD);
        }
    }

    // 비밀번호 변경
    public void changePassword(String newEncodedPassword) {
        if (newEncodedPassword == null || newEncodedPassword.isBlank()) {
            throw new CustomException(AUTH_PASSWORD_REQUIRED);
        }
        this.password = newEncodedPassword;
    }

    private static void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new CustomException(AUTH_EMAIL_REQUIRED);
        }
        if (!email.contains("@")) {
            throw new CustomException(AUTH_EMAIL_INVALID_FORMAT);
        }
    }
}
