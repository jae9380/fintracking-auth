package com.ft.auth.domain;

import com.ft.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static com.ft.common.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("RefreshToken 도메인 테스트")
class RefreshTokenTest {

    private static final LocalDateTime FUTURE = LocalDateTime.now().plusDays(7);
    private static final LocalDateTime PAST = LocalDateTime.now().minusDays(1);

    @Nested
    @DisplayName("토큰 생성")
    class Create {

        @Test
        @DisplayName("성공 - 유효한 파라미터로 생성된다")
        void success_validParams_createsToken() {
            // given
            Long userId = 1L;
            String tokenValue = "token-value";

            // when
            RefreshToken token = RefreshToken.create(userId, tokenValue, FUTURE);

            // then
            assertThat(token.getUserId()).isEqualTo(1L);
            assertThat(token.getToken()).isEqualTo("token-value");
            assertThat(token.getExpiresAt()).isEqualTo(FUTURE);
        }

        @Test
        @DisplayName("실패 - 토큰값이 공백이면 예외 발생")
        void fail_blankToken_throwsException() {
            // given
            String blankToken = "  ";

            // when & then
            assertThatThrownBy(() -> RefreshToken.create(1L, blankToken, FUTURE))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_REFRESH_TOKEN_REQUIRED));
        }

        @Test
        @DisplayName("실패 - 토큰값이 null이면 예외 발생")
        void fail_nullToken_throwsException() {
            // when & then
            assertThatThrownBy(() -> RefreshToken.create(1L, null, FUTURE))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_REFRESH_TOKEN_REQUIRED));
        }

        @Test
        @DisplayName("실패 - 만료 시각이 과거이면 예외 발생")
        void fail_pastExpiresAt_throwsException() {
            // given
            LocalDateTime pastTime = PAST;

            // when & then
            assertThatThrownBy(() -> RefreshToken.create(1L, "token-value", pastTime))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_REFRESH_TOKEN_INVALID));
        }

        @Test
        @DisplayName("실패 - 만료 시각이 null이면 예외 발생")
        void fail_nullExpiresAt_throwsException() {
            // when & then
            assertThatThrownBy(() -> RefreshToken.create(1L, "token-value", null))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_REFRESH_TOKEN_INVALID));
        }
    }

    @Nested
    @DisplayName("만료 여부")
    class IsExpired {

        @Test
        @DisplayName("성공 - 미래 만료 시각이면 만료되지 않음")
        void success_futureExpiry_returnsFalse() {
            // given
            RefreshToken token = RefreshToken.create(1L, "token-value", FUTURE);

            // when
            boolean expired = token.isExpired();

            // then
            assertThat(expired).isFalse();
        }
    }

    @Nested
    @DisplayName("토큰 검증")
    class ValidateToken {

        @Test
        @DisplayName("성공 - 토큰값이 일치하면 예외 없음")
        void success_matchingToken_noException() {
            // given
            RefreshToken token = RefreshToken.create(1L, "token-value", FUTURE);

            // when & then
            assertThatNoException().isThrownBy(() -> token.validateToken("token-value"));
        }

        @Test
        @DisplayName("실패 - 토큰값이 다르면 예외 발생")
        void fail_mismatchedToken_throwsException() {
            // given
            RefreshToken token = RefreshToken.create(1L, "token-value", FUTURE);

            // when & then
            assertThatThrownBy(() -> token.validateToken("wrong-token"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_REFRESH_TOKEN_INVALID));
        }
    }

    @Nested
    @DisplayName("토큰 Rotation")
    class Rotate {

        @Test
        @DisplayName("성공 - 새 토큰값과 만료 시각으로 갱신된다")
        void success_validNewToken_updatesTokenAndExpiry() {
            // given
            RefreshToken token = RefreshToken.create(1L, "old-token", FUTURE);
            LocalDateTime newExpiry = LocalDateTime.now().plusDays(14);

            // when
            token.rotate("new-token", newExpiry);

            // then
            assertThat(token.getToken()).isEqualTo("new-token");
            assertThat(token.getExpiresAt()).isEqualTo(newExpiry);
        }

        @Test
        @DisplayName("실패 - 새 토큰값이 공백이면 예외 발생")
        void fail_blankNewToken_throwsException() {
            // given
            RefreshToken token = RefreshToken.create(1L, "old-token", FUTURE);

            // when & then
            assertThatThrownBy(() -> token.rotate("  ", FUTURE))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_NEW_REFRESH_TOKEN_REQUIRED));
        }
    }
}
