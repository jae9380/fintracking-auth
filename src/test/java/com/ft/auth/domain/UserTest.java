package com.ft.auth.domain;

import com.ft.common.exception.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.ft.common.exception.ErrorCode.*;
import static org.assertj.core.api.Assertions.*;

@DisplayName("User 도메인 테스트")
class UserTest {

    @Nested
    @DisplayName("유저 생성")
    class Create {

        @Test
        @DisplayName("성공 - 유효한 이메일과 비밀번호로 생성된다")
        void success_validEmailAndPassword_createsUser() {
            // given
            String email = "test@example.com";
            String encodedPassword = "encoded_password";

            // when
            User user = User.create(email, encodedPassword);

            // then
            assertThat(user.getEmail()).isEqualTo("test@example.com");
            assertThat(user.getPassword()).isEqualTo("encoded_password");
            assertThat(user.getProvider()).isEqualTo(OAuth2Provider.EMAIL);
        }

        @Test
        @DisplayName("실패 - 이메일이 null이면 예외 발생")
        void fail_nullEmail_throwsException() {
            // given
            String nullEmail = null;

            // when & then
            assertThatThrownBy(() -> User.create(nullEmail, "encoded_password"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_EMAIL_REQUIRED));
        }

        @Test
        @DisplayName("실패 - 이메일이 공백이면 예외 발생")
        void fail_blankEmail_throwsException() {
            // given
            String blankEmail = "   ";

            // when & then
            assertThatThrownBy(() -> User.create(blankEmail, "encoded_password"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_EMAIL_REQUIRED));
        }

        @Test
        @DisplayName("실패 - 이메일 형식이 잘못되면 예외 발생")
        void fail_invalidEmailFormat_throwsException() {
            // given
            String invalidEmail = "not-an-email";

            // when & then
            assertThatThrownBy(() -> User.create(invalidEmail, "encoded_password"))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_EMAIL_INVALID_FORMAT));
        }
    }

    @Nested
    @DisplayName("OAuth2 유저 생성")
    class CreateOAuth2User {

        @Test
        @DisplayName("성공 - KAKAO 프로바이더로 생성된다")
        void success_kakaoProvider_createsOAuth2User() {
            // given
            String email = "kakao@example.com";
            OAuth2Provider provider = OAuth2Provider.KAKAO;

            // when
            User user = User.createOAuth2User(email, provider);

            // then
            assertThat(user.getEmail()).isEqualTo("kakao@example.com");
            assertThat(user.getPassword()).isNull();
            assertThat(user.getProvider()).isEqualTo(OAuth2Provider.KAKAO);
        }

        @Test
        @DisplayName("실패 - EMAIL 프로바이더이면 예외 발생")
        void fail_emailProvider_throwsException() {
            // given
            OAuth2Provider emailProvider = OAuth2Provider.EMAIL;

            // when & then
            assertThatThrownBy(() -> User.createOAuth2User("test@example.com", emailProvider))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_OAUTH2_FAILED));
        }

        @Test
        @DisplayName("실패 - 프로바이더가 null이면 예외 발생")
        void fail_nullProvider_throwsException() {
            // when & then
            assertThatThrownBy(() -> User.createOAuth2User("test@example.com", null))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_OAUTH2_FAILED));
        }
    }

    @Nested
    @DisplayName("비밀번호 검증")
    class ValidatePassword {

        @Test
        @DisplayName("성공 - 비밀번호가 일치하면 예외 없음")
        void success_correctPassword_noException() {
            // given
            User user = User.create("test@example.com", "encoded");
            PasswordValidator validator = (raw, encoded) -> raw.equals("correct") && encoded.equals("encoded");

            // when & then
            assertThatNoException().isThrownBy(() -> user.validatePassword("correct", validator));
        }

        @Test
        @DisplayName("실패 - 비밀번호가 틀리면 예외 발생")
        void fail_wrongPassword_throwsException() {
            // given
            User user = User.create("test@example.com", "encoded");
            PasswordValidator validator = (raw, encoded) -> false;

            // when & then
            assertThatThrownBy(() -> user.validatePassword("wrong", validator))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_INVALID_PASSWORD));
        }
    }

    @Nested
    @DisplayName("비밀번호 변경")
    class ChangePassword {

        @Test
        @DisplayName("성공 - 비밀번호가 변경된다")
        void success_validNewPassword_changesPassword() {
            // given
            User user = User.create("test@example.com", "old_encoded");
            String newEncodedPassword = "new_encoded";

            // when
            user.changePassword(newEncodedPassword);

            // then
            assertThat(user.getPassword()).isEqualTo("new_encoded");
        }

        @Test
        @DisplayName("실패 - 새 비밀번호가 공백이면 예외 발생")
        void fail_blankNewPassword_throwsException() {
            // given
            User user = User.create("test@example.com", "old_encoded");
            String blankPassword = "   ";

            // when & then
            assertThatThrownBy(() -> user.changePassword(blankPassword))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_PASSWORD_REQUIRED));
        }

        @Test
        @DisplayName("실패 - 새 비밀번호가 null이면 예외 발생")
        void fail_nullNewPassword_throwsException() {
            // given
            User user = User.create("test@example.com", "old_encoded");

            // when & then
            assertThatThrownBy(() -> user.changePassword(null))
                    .isInstanceOf(CustomException.class)
                    .satisfies(e -> assertThat(((CustomException) e).getErrorCode())
                            .isEqualTo(AUTH_PASSWORD_REQUIRED));
        }
    }
}
