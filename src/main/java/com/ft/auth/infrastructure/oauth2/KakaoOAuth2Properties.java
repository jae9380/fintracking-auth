package com.ft.auth.infrastructure.oauth2;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * kakao.* 설정 바인딩
 * application.yml의 kakao.client-id, kakao.redirect-uri를 주입받는다.
 */
@Getter
@Setter  // ConfigurationProperties 바인딩에만 사용 — 외부에서 직접 호출 금지
@Component
@ConfigurationProperties(prefix = "kakao")
public class KakaoOAuth2Properties {

    private String clientId;
    private String redirectUri;
}
