# fintracking-auth

회원가입/로그인, JWT 발급, Refresh Token Rotation

---

## 패턴: Template Method

```
AbstractAuthHandler (추상)
  ├── EmailAuthHandler
  └── KakaoAuthHandler (OAuth2)
```

새 인증 방식 추가 시 `AbstractAuthHandler` 상속으로만 확장.

---

## JWT 구조

- **Access Token**: 1시간 만료 (config: `jwt.access-expiration: 3600000`)
- **Refresh Token**: 7일 만료 (config: `jwt.refresh-expiration: 604800000`)
- **Secret 위치**: `application-secret.yml` (공통) `jwt.secret` — auth/gateway 양쪽 공유
- Refresh Token Rotation 적용 (재발급 시 기존 무효화)

---

## Config 로딩 순서

Config 서버에서 다음 순서로 로드:
1. `application-secret.yml` → `jwt.secret`, `spring.datasource.password`
2. `fintracking-auth.yml` → `jwt.access-expiration`, `jwt.refresh-expiration`, DB URL

---

## 보안

- 비밀번호: BCrypt 해싱
- 개인정보: AES-256 암호화 대상 (민감 필드)
- `/auth-service/**` 경로는 Gateway에서 AuthorizationHeaderFilter 미적용 (공개 엔드포인트)

---

## 패키지 구조

```
com.ft.auth
  ├── domain/          — User, Token 엔티티
  ├── application/     — AuthService, TokenService
  ├── infrastructure/  — JPA, OAuth2 클라이언트
  └── presentation/    — AuthController, DTO
```

---

## 주요 ErrorCode

```java
INVALID_TOKEN(401, "AUTH_001", "Invalid token")
EXPIRED_TOKEN(401, "AUTH_002", "Expired token")
USER_NOT_FOUND(404, "AUTH_003", "User not found")
DUPLICATE_EMAIL(409, "AUTH_004", "Email already exists")
```
