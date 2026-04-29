package com.ft.auth.infrastructure.oauth2;

import com.ft.auth.application.dto.KakaoUserInfo;
import com.ft.auth.infrastructure.oauth2.dto.KakaoTokenResponse;
import com.ft.auth.infrastructure.oauth2.dto.KakaoUserResponse;
import com.ft.common.exception.CustomException;
import com.ft.common.metric.helper.ExternalApiMetricHelper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KakaoOAuth2ClientTest {

    @Mock
    private RestTemplate restTemplate;

    private KakaoOAuth2Properties kakaoOAuth2Properties;
    private MeterRegistry meterRegistry;
    private ExternalApiMetricHelper metricHelper;
    private KakaoOAuth2Client sut;

    @BeforeEach
    void setUp() {
        kakaoOAuth2Properties = new KakaoOAuth2Properties();
        kakaoOAuth2Properties.setClientId("test-client-id");
        kakaoOAuth2Properties.setRedirectUri("http://localhost/callback");

        meterRegistry = new SimpleMeterRegistry();
        metricHelper = new ExternalApiMetricHelper(meterRegistry);

        sut = new KakaoOAuth2Client(restTemplate, kakaoOAuth2Properties, metricHelper);
    }

    // ---- getAccessToken ----

    @Test
    @DisplayName("getAccessToken_성공_accessToken반환_success카운터1증가")
    void getAccessToken_success_returnsTokenAndIncrementsSuccessCounter() {
        // given
        KakaoTokenResponse tokenResponse = new KakaoTokenResponse();
        setField(tokenResponse, "accessToken", "kakao-access-token-abc");

        when(restTemplate.postForObject(any(String.class), any(), eq(KakaoTokenResponse.class)))
                .thenReturn(tokenResponse);

        // when
        String result = sut.getAccessToken("auth-code-123");

        // then
        assertThat(result).isEqualTo("kakao-access-token-abc");

        double successCount = meterRegistry.get("ft_external_api_requests_total")
                .tag("system", "kakao")
                .tag("operation", "get_access_token")
                .tag("result", "success")
                .counter().count();
        assertThat(successCount).isEqualTo(1.0);
    }

    @Test
    @DisplayName("getAccessToken_성공_duration타이머기록")
    void getAccessToken_success_recordsDuration() {
        // given
        KakaoTokenResponse tokenResponse = new KakaoTokenResponse();
        setField(tokenResponse, "accessToken", "token");
        when(restTemplate.postForObject(any(String.class), any(), eq(KakaoTokenResponse.class)))
                .thenReturn(tokenResponse);

        // when
        sut.getAccessToken("code");

        // then
        long timerCount = meterRegistry.get("ft_external_api_duration_seconds")
                .tag("system", "kakao")
                .tag("operation", "get_access_token")
                .timer().count();
        assertThat(timerCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("getAccessToken_응답null_CustomException발생")
    void getAccessToken_nullResponse_throwsCustomException() {
        // given
        when(restTemplate.postForObject(any(String.class), any(), eq(KakaoTokenResponse.class)))
                .thenReturn(null);

        // when & then
        assertThatThrownBy(() -> sut.getAccessToken("code"))
                .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("getAccessToken_HttpClientError_fail카운터1증가_CustomException재전파")
    void getAccessToken_httpClientError_incrementsFailCounterAndThrows() {
        // given
        when(restTemplate.postForObject(any(String.class), any(), eq(KakaoTokenResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // when & then
        assertThatThrownBy(() -> sut.getAccessToken("bad-code"))
                .isInstanceOf(CustomException.class);

        double failCount = meterRegistry.get("ft_external_api_requests_total")
                .tag("system", "kakao")
                .tag("operation", "get_access_token")
                .tag("result", "fail")
                .tag("error_type", "HttpClientErrorException")
                .counter().count();
        assertThat(failCount).isEqualTo(1.0);
    }

    @Test
    @DisplayName("getAccessToken_실패시에도_duration타이머_finally에서기록")
    void getAccessToken_fail_stillRecordsDurationInFinally() {
        // given
        when(restTemplate.postForObject(any(String.class), any(), eq(KakaoTokenResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // when
        try { sut.getAccessToken("bad-code"); } catch (CustomException ignored) {}

        // then
        long timerCount = meterRegistry.get("ft_external_api_duration_seconds")
                .tag("system", "kakao")
                .tag("operation", "get_access_token")
                .timer().count();
        assertThat(timerCount).isEqualTo(1L);
    }

    // ---- getUserInfo ----

    @Test
    @DisplayName("getUserInfo_성공_KakaoUserInfo반환_success카운터1증가")
    void getUserInfo_success_returnsUserInfoAndIncrementsSuccessCounter() {
        // given
        KakaoUserResponse.KakaoAccount account = new KakaoUserResponse.KakaoAccount();
        setField(account, "email", "user@kakao.com");

        KakaoUserResponse userResponse = new KakaoUserResponse();
        setField(userResponse, "id", 12345L);
        setField(userResponse, "kakaoAccount", account);

        when(restTemplate.exchange(any(String.class), any(), any(), eq(KakaoUserResponse.class)))
                .thenReturn(ResponseEntity.ok(userResponse));

        // when
        KakaoUserInfo result = sut.getUserInfo("access-token");

        // then
        assertThat(result.email()).isEqualTo("user@kakao.com");

        double successCount = meterRegistry.get("ft_external_api_requests_total")
                .tag("system", "kakao")
                .tag("operation", "get_user_info")
                .tag("result", "success")
                .counter().count();
        assertThat(successCount).isEqualTo(1.0);
    }

    @Test
    @DisplayName("getUserInfo_HttpClientError_fail카운터1증가_CustomException재전파")
    void getUserInfo_httpClientError_incrementsFailCounterAndThrows() {
        // given
        when(restTemplate.exchange(any(String.class), any(), any(), eq(KakaoUserResponse.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.UNAUTHORIZED));

        // when & then
        assertThatThrownBy(() -> sut.getUserInfo("bad-token"))
                .isInstanceOf(CustomException.class);

        double failCount = meterRegistry.get("ft_external_api_requests_total")
                .tag("system", "kakao")
                .tag("operation", "get_user_info")
                .tag("result", "fail")
                .counter().count();
        assertThat(failCount).isEqualTo(1.0);
    }

    // ---- utility ----

    private void setField(Object target, String fieldName, Object value) {
        try {
            var field = findField(target.getClass(), fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field: " + fieldName, e);
        }
    }

    private java.lang.reflect.Field findField(Class<?> clazz, String fieldName) {
        Class<?> current = clazz;
        while (current != null) {
            try {
                return current.getDeclaredField(fieldName);
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass();
            }
        }
        throw new RuntimeException("Field not found: " + fieldName);
    }
}
