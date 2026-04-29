package com.ft.auth.infrastructure.oauth2;

import com.ft.auth.application.dto.KakaoUserInfo;
import com.ft.auth.application.port.KakaoOAuth2Port;
import com.ft.auth.infrastructure.oauth2.dto.KakaoTokenResponse;
import com.ft.auth.infrastructure.oauth2.dto.KakaoUserResponse;
import com.ft.common.exception.CustomException;
import com.ft.common.metric.helper.ExternalApiMetricHelper;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static com.ft.common.exception.ErrorCode.AUTH_OAUTH2_FAILED;

/**
 * 카카오 OAuth2 외부 API 클라이언트 (Infrastructure)
 * KakaoOAuth2Port를 구현하며, RestTemplate으로 카카오 서버와 통신한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class KakaoOAuth2Client implements KakaoOAuth2Port {

    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    private static final String SYSTEM = "kakao";

    private final RestTemplate restTemplate;
    private final KakaoOAuth2Properties kakaoOAuth2Properties;
    private final ExternalApiMetricHelper metricHelper;

    /**
     * 인가 코드 → 카카오 Access Token 교환
     * POST https://kauth.kakao.com/oauth/token
     */
    @Override
    public String getAccessToken(String code) {
        Timer.Sample sample = metricHelper.startSample();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("grant_type", GRANT_TYPE_AUTHORIZATION_CODE);
            body.add("client_id", kakaoOAuth2Properties.getClientId());
            body.add("redirect_uri", kakaoOAuth2Properties.getRedirectUri());
            body.add("code", code);

            KakaoTokenResponse response = restTemplate.postForObject(
                    KAKAO_TOKEN_URL,
                    new HttpEntity<>(body, headers),
                    KakaoTokenResponse.class
            );

            if (response == null || response.getAccessToken() == null) {
                log.error("카카오 토큰 발급 실패: 응답이 null");
                throw new CustomException(AUTH_OAUTH2_FAILED);
            }

            metricHelper.success(SYSTEM, "get_access_token").increment();
            return response.getAccessToken();

        } catch (HttpClientErrorException e) {
            metricHelper.fail(SYSTEM, "get_access_token", "HttpClientErrorException").increment();
            log.error("카카오 토큰 발급 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(AUTH_OAUTH2_FAILED);

        } finally {
            sample.stop(metricHelper.timer(SYSTEM, "get_access_token"));
        }
    }

    /**
     * 카카오 Access Token → 유저 정보 조회
     * GET https://kapi.kakao.com/v2/user/me
     */
    @Override
    public KakaoUserInfo getUserInfo(String accessToken) {
        Timer.Sample sample = metricHelper.startSample();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);

            KakaoUserResponse response = restTemplate.exchange(
                    KAKAO_USER_INFO_URL,
                    HttpMethod.GET,
                    new HttpEntity<>(headers),
                    KakaoUserResponse.class
            ).getBody();

            if (response == null) {
                log.error("카카오 유저 정보 조회 실패: 응답이 null");
                throw new CustomException(AUTH_OAUTH2_FAILED);
            }

            metricHelper.success(SYSTEM, "get_user_info").increment();
            return response.toKakaoUserInfo();

        } catch (HttpClientErrorException e) {
            metricHelper.fail(SYSTEM, "get_user_info", "HttpClientErrorException").increment();
            log.error("카카오 유저 정보 조회 실패: status={}, body={}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new CustomException(AUTH_OAUTH2_FAILED);

        } finally {
            sample.stop(metricHelper.timer(SYSTEM, "get_user_info"));
        }
    }
}
