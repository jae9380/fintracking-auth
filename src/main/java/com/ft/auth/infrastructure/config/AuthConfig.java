package com.ft.auth.infrastructure.config;

import com.ft.auth.domain.PasswordValidator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AuthConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public PasswordValidator passwordValidator(PasswordEncoder passwordEncoder) {
        return passwordEncoder::matches;
    }

    // 카카오 OAuth2 API 호출용 RestTemplate
    // 운영 환경에서는 타임아웃 설정 추가 권장 (HttpComponentsClientHttpRequestFactory)
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
