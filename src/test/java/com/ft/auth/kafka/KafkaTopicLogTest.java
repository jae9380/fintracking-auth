package com.ft.auth.kafka;

import com.ft.auth.application.UserRegisteredEventPublisher;
import com.ft.common.event.UserRegisteredEvent;
import com.ft.common.kafka.KafkaTopic;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

/**
 * Kafka 토픽 발행 로그 테스트 — auth-service
 *
 * 실제 Kafka 브로커 없이 KafkaTemplate을 Mock 처리하고,
 * ArgumentCaptor로 실제 발행될 토픽명과 페이로드를 캡처하여 콘솔에 출력한다.
 */
@Slf4j
@ExtendWith(MockitoExtension.class)
@DisplayName("Kafka 토픽 발행 로그 테스트 - auth-service")
class KafkaTopicLogTest {

    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;

    UserRegisteredEventPublisher publisher;

    @BeforeEach
    void setUp() {
        given(kafkaTemplate.send(anyString(), any())).willReturn(null);
        publisher = new UserRegisteredEventPublisher(kafkaTemplate);
    }

    @Test
    @DisplayName("성공 - UserRegisteredEvent 발행 시 user.registered 토픽으로 전송된다")
    void publish_whenUserRegistered_sendsToCorrectTopic() {
        // given
        UserRegisteredEvent event = new UserRegisteredEvent("evt-001", 1L, "user@example.com");

        // when
        publisher.publish(event);

        // then
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Object> payloadCaptor = ArgumentCaptor.forClass(Object.class);
        then(kafkaTemplate).should().send(topicCaptor.capture(), payloadCaptor.capture());

        log.info("┌─────────────────────────────────────────────────");
        log.info("│ [KAFKA-LOG] auth-service → PUBLISH");
        log.info("│ topic   : {}", topicCaptor.getValue());
        log.info("│ payload : {}", payloadCaptor.getValue());
        log.info("└─────────────────────────────────────────────────");

        assertThat(topicCaptor.getValue()).isEqualTo(KafkaTopic.USER_REGISTERED);
        assertThat(payloadCaptor.getValue()).isInstanceOf(UserRegisteredEvent.class);
    }
}
