package com.ft.auth.infrastructure.persistence;

import com.ft.auth.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DisplayName("JpaUserRepository 테스트")
class JpaUserRepositoryTest {

    @Autowired
    JpaUserRepository jpaUserRepository;

    @Nested
    @DisplayName("이메일로 유저 조회")
    class FindByEmail {

        @Test
        @DisplayName("성공 - 존재하는 이메일이면 유저를 반환한다")
        void findByEmail_whenExists_returnsUser() {
            // given
            User user = User.create("test@example.com", "encoded_password");
            jpaUserRepository.save(user);

            // when
            Optional<User> result = jpaUserRepository.findByEmail("test@example.com");

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 이메일이면 empty를 반환한다")
        void findByEmail_whenNotExists_returnsEmpty() {
            // when
            Optional<User> result = jpaUserRepository.findByEmail("nobody@example.com");

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("이메일 존재 여부 확인")
    class ExistsByEmail {

        @Test
        @DisplayName("성공 - 존재하는 이메일이면 true를 반환한다")
        void existsByEmail_whenExists_returnsTrue() {
            // given
            User user = User.create("exists@example.com", "encoded_password");
            jpaUserRepository.save(user);

            // when
            boolean exists = jpaUserRepository.existsByEmail("exists@example.com");

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("성공 - 존재하지 않는 이메일이면 false를 반환한다")
        void existsByEmail_whenNotExists_returnsFalse() {
            // when
            boolean exists = jpaUserRepository.existsByEmail("nobody@example.com");

            // then
            assertThat(exists).isFalse();
        }
    }
}
