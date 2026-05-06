# fintracking-auth

> 회원가입 · 로그인 · JWT 발급 · Refresh Token Rotation

---

## 사용 기술

|    분류     |             기술             |
| :---------: | :--------------------------: |
|    보안     | Spring Security, BCrypt, JWT |
|  설정 관리  |     Spring Cloud Config      |
| 서비스 등록 |  Spring Cloud Eureka Client  |
|   문서화    | SpringDoc OpenAPI (Swagger)  |

---

## 핵심 설계 패턴 — Template Method

### 문제 상황

이메일 로그인과 소셜(kakao) 로그인은 **흐름은 동일하지만 세부 구현이 다르다**

```
공통 흐름:
  1. 사용자 조회
  2. 자격증명 검증
  3. 토큰 발급
```

### 해결 — Template Method 패턴

동일 흐름을 중복 구현을 피하기 위해 템플릿 메소드 패턴을 사용.

부모 클래스가 **흐름을 지정**하고, 자식 클래스가 **세부 구현을 한다.**

```
AbstractAuthHandler (추상 부모)
│
│  login() ← 외부에서 호출하는 메서드 (final — 변경 불가)
│   ├── 1. loadUser()          ← 추상 메서드
│   ├── 2. verifyCredentials() ← 추상 메서드
│   └── 3. issueTokens()       ← 공통 구현
│
├── EmailAuthHandler
│     ├── loadUser()         → DB에서 이메일로 조회
│     └── verifyCredentials() → BCrypt 비밀번호 비교
│
└── KakaoAuthHandler
      ├── loadUser()         → 카카오 API로 사용자 정보 조회
      └── verifyCredentials() → 카카오 토큰 유효성 확인
```

**효과:** 추가적인 로그인 방식(네이버, 구글 등)을 추가할 때 `AbstractAuthHandler`만 상속하면 된다. 그렇기 떄문에 기존 코드에 대한 수정은 피할 수 있다.

```java
// 부모 클래스 — 흐름을 고정
public abstract class AbstractAuthHandler {

    // final: 자식이 재정의 불가 → 흐름 보장
    public final LoginResult login(LoginCommand command) {
        User user = loadUser(command);          // 자식이 구현
        verifyCredentials(user, command);       // 자식이 구현
        return issueTokens(user);               // 공통 로직
    }

    protected abstract User loadUser(LoginCommand command);
    protected abstract void verifyCredentials(User user, LoginCommand command);
}
```

---

## JWT 인증 구조

```
[클라이언트]
     │
     │  POST /auth-service/api/v1/auth/login
     │  { email, password }
     ▼
[Gateway] ← auth 경로는 JWT 검증 없이 통과
     │
     ▼
[auth-service]
     │  1. 이메일/비밀번호 검증
     │  2. Access Token (1시간) 발급
     │  3. Refresh Token (7일) 발급 + DB 적재
     ▼
[클라이언트]
     │  ← { accessToken, refreshToken }
     │
     │  이후 API 요청 시: Authorization: Bearer {accessToken}
     │
     │  Access Token 만료 시:
     │  POST /auth-service/api/v1/auth/reissue
     │  Authorization: Bearer {refreshToken}
     ▼
[auth-service]
     │  1. Refresh Token 유효성 확인
     │  2. 기존 Refresh Token 삭제 (Rotation)
     │  3. 새 Access Token + Refresh Token 발급
```

### Refresh Token Rotation

보안을 위해 Refresh Token을 한 번 사용하면 즉시 폐기하고 새 토큰을 발급한다.
이렇게 하면 토큰이 탈취되더라도 피해를 최소화할 수 있다.

```
재로그인 또는 재발급 시:
  기존 Refresh Token → 삭제
  새 Refresh Token  → DB 저장
```

---

## 패키지 구조

```
com.ft.auth
├── domain/
│   ├── User.java               ← 사용자 엔티티 (BCrypt 비밀번호 저장)
│   ├── RefreshToken.java       ← Refresh Token 엔티티 (만료 시간 포함)
│   ├── PasswordValidator.java  ← 도메인 비밀번호 검증 규칙
│   └── OAuth2Provider.java     ← 소셜 로그인 제공자 enum
│
├── application/
│   ├── AuthService.java             ← 회원가입/로그인/재발급 유스케이스
│   ├── handler/
│   │   ├── AbstractAuthHandler.java  ← Template Method 추상 클래스
│   │   ├── EmailAuthHandler.java     ← 이메일 로그인 구현
│   │   └── KakaoAuthHandler.java     ← 카카오 OAuth2 로그인 구현
│   ├── port/
│   │   ├── UserRepository.java          ← DB 접근 추상화 인터페이스
│   │   ├── RefreshTokenRepository.java  ← 토큰 저장소 인터페이스
│   │   ├── TokenProvider.java           ← JWT 발급 인터페이스
│   │   └── KakaoOAuth2Port.java         ← 카카오 API 인터페이스
│   └── dto/                         ← Command/Result 내부 DTO
│
├── infrastructure/
│   ├── jwt/
│   │   ├── JwtTokenProvider.java        ← JWT 생성/파싱 구현체
│   │   └── JwtAuthenticationFilter.java ← Spring Security 필터
│   ├── oauth2/
│   │   └── KakaoOAuth2Client.java       ← 카카오 API HTTP 클라이언트
│   ├── persistence/
│   │   ├── JpaUserRepository.java            ← JPA 인터페이스
│   │   ├── UserRepositoryImpl.java           ← UserRepository 구현
│   │   ├── JpaRefreshTokenRepository.java    ← JPA 인터페이스
│   │   └── RefreshTokenRepositoryImpl.java   ← RefreshTokenRepository 구현
│   └── config/
│       ├── SecurityConfig.java  ← Spring Security 설정
│       └── AuthConfig.java      ← BCryptPasswordEncoder 빈 등록
│
└── presentation/
    ├── AuthController.java    ← REST API 엔드포인트
    └── dto/                   ← Request/Response DTO
```

---

## API 엔드포인트

| 메서드 | 경로                                     | 설명                 | 인증 필요         |
| ------ | ---------------------------------------- | -------------------- | ----------------- |
| POST   | `/auth-service/api/v1/auth/signup`       | 이메일 회원가입      | ✗                 |
| POST   | `/auth-service/api/v1/auth/login`        | 이메일 로그인        | ✗                 |
| POST   | `/auth-service/api/v1/auth/reissue`      | Access Token 재발급  | ✗ (Refresh Token) |
| POST   | `/auth-service/api/v1/auth/oauth2/login` | 카카오 OAuth2 로그인 | ✗                 |

---

## 보안 설정

- **비밀번호**: BCrypt 해싱 (단방향, 복호화 불가)
- **JWT Secret**: Config 서버의 `application-secret.yml` → `jwt.secret` 에서 로드
- **Gateway와 Secret 공유**: Gateway도 동일한 Secret으로 토큰 서명 검증

```yaml
# application-secret.yml (Config 서버 관리)
jwt:
  secret: "{cipher}암호화된값" # AES 암호화 저장
  access-expiration: 3600000 # 1시간 (ms)
  refresh-expiration: 604800000 # 7일 (ms)
```

---

## 에러 코드

| 코드                         | HTTP | 설명                        |
| ---------------------------- | ---- | --------------------------- |
| `AUTH_EMAIL_EXISTS`          | 409  | 이미 존재하는 이메일        |
| `AUTH_USER_NOT_FOUND`        | 404  | 사용자를 찾을 수 없음       |
| `AUTH_INVALID_PASSWORD`      | 401  | 비밀번호 불일치             |
| `AUTH_REFRESH_TOKEN_INVALID` | 401  | 유효하지 않은 Refresh Token |
| `AUTH_REFRESH_TOKEN_EXPIRED` | 401  | 만료된 Refresh Token        |
| `AUTH_OAUTH2_FAILED`         | 400  | 카카오 OAuth2 인증 실패     |

---

## 테스트

```
test/
├── domain/
│   ├── UserTest.java           ← 비밀번호 검증, 이메일 형식 단위 테스트
│   └── RefreshTokenTest.java   ← 만료 시간 검증 단위 테스트
├── application/
│   └── AuthServiceTest.java    ← 회원가입/로그인 유스케이스 (Mockito)
├── infrastructure/
│   ├── JpaUserRepositoryTest.java         ← @DataJpaTest + H2
│   └── KakaoOAuth2ClientTest.java         ← Kakao API Mock 테스트
└── kafka/
    └── KafkaTopicLogTest.java  ← 토픽 상수 검증
```
