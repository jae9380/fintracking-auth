package com.ft.auth.application;

import com.ft.auth.application.dto.SignupCommand;
import com.ft.auth.application.handler.EmailAuthHandler;
import com.ft.auth.application.handler.KakaoAuthHandler;
import com.ft.auth.application.port.RefreshTokenRepository;
import com.ft.auth.application.port.TokenProvider;
import com.ft.auth.application.port.UserRepository;
import com.ft.auth.domain.RefreshToken;
import com.ft.auth.domain.User;
import com.ft.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.ft.common.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @Mock UserRepository userRepository;
    @Mock RefreshTokenRepository refreshTokenRepository;
    @Mock TokenProvider tokenProvider;
    @Mock EmailAuthHandler emailAuthHandler;
    @Mock KakaoAuthHandler kakaoAuthHandler;
    @Mock PasswordEncoder passwordEncoder;
    @Mock UserRegisteredEventPublisher userRegisteredEventPublisher;

    @InjectMocks AuthService authService;

    @Nested
    @DisplayName("회원가입")
    class Signup {

        @Test
        @DisplayName("성공 - 신규 이메일이면 저장 후 이벤트를 발행한다")
        void signup_whenNewEmail_savesUserAndPublishesEvent() {
            // given
            SignupCommand command = new SignupCommand("new@example.com", "raw_password");
            User savedUser = User.create("new@example.com", "encoded");
            given(userRepository.existsByEmail("new@example.com")).willReturn(false);
            given(passwordEncoder.encode("raw_password")).willReturn("encoded");
            given(userRepository.save(any(User.class))).willReturn(savedUser);

            // when
            authService.signup(command);

            // then
            then(userRepository).should().save(any(User.class));
            then(userRegisteredEventPublisher).should().publish(any());
        }

        @Test
        @DisplayName("실패 - 이메일 중복이면 예외 발생")
        void signup_whenDuplicateEmail_throwsCustomException() {
            // given
            SignupCommand command = new SignupCommand("exists@example.com", "raw_password");
            given(userRepository.existsByEmail("exists@example.com")).willReturn(true);

            // when & then
            assertThatThrownBy(() -> authService.signup(command))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_EMAIL_EXISTS));
        }
    }

    @Nested
    @DisplayName("토큰 재발급")
    class Reissue {

        @Test
        @DisplayName("성공 - 유효한 토큰이면 새 AccessToken을 반환한다")
        void reissue_whenValidToken_returnsNewAccessToken() {
            // given
            String rawToken = "valid-refresh-token";
            RefreshToken refreshToken = RefreshToken.create(1L, rawToken, LocalDateTime.now().plusDays(7));
            given(refreshTokenRepository.findByToken(rawToken)).willReturn(Optional.of(refreshToken));
            given(tokenProvider.createAccessToken(1L)).willReturn("new-access-token");
            given(tokenProvider.createRefreshToken(1L)).willReturn("new-refresh-token");
            given(tokenProvider.getRefreshTokenExpiry()).willReturn(LocalDateTime.now().plusDays(7));

            // when
            String newAccessToken = authService.reissue(rawToken);

            // then
            assertThat(newAccessToken).isEqualTo("new-access-token");
            then(refreshTokenRepository).should().save(any(RefreshToken.class));
        }

        @Test
        @DisplayName("실패 - 토큰이 존재하지 않으면 예외 발생")
        void reissue_whenTokenNotFound_throwsCustomException() {
            // given
            given(refreshTokenRepository.findByToken(anyString())).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> authService.reissue("invalid-token"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_INVALID_TOKEN));
        }
    }

    @Nested
    @DisplayName("로그아웃")
    class Logout {

        @Test
        @DisplayName("성공 - RefreshToken 삭제를 호출한다")
        void logout_whenCalled_deletesRefreshToken() {
            // given
            Long userId = 1L;

            // when
            authService.logout(userId);

            // then
            then(refreshTokenRepository).should().deleteByUserId(userId);
        }
    }
}
