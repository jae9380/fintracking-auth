package com.ft.auth.application;

import com.ft.common.event.UserRegisteredEvent;
import com.ft.common.kafka.AbstractEventPublisher;
import com.ft.common.kafka.KafkaTopic;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserRegisteredEventPublisher extends AbstractEventPublisher<UserRegisteredEvent> {

    public UserRegisteredEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        super(kafkaTemplate);
    }

    @Override
    public String topic() {
        return KafkaTopic.USER_REGISTERED;
    }
}
