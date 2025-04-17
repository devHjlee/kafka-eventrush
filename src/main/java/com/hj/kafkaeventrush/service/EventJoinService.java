package com.hj.kafkaeventrush.service;

import com.hj.kafkaeventrush.dto.EventJoinMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class EventJoinService {

    private final KafkaTemplate<String, EventJoinMessage> kafkaTemplate;

    public void requestJoin(Long userId) {
        EventJoinMessage message = new EventJoinMessage(userId, LocalDateTime.now());
        kafkaTemplate.send("event.join", message);
    }
}
