package com.hj.kafkaeventrush.service;

import com.hj.kafkaeventrush.dto.EventJoinMessage;
import com.hj.kafkaeventrush.entity.EventJoin;
import com.hj.kafkaeventrush.entity.JoinResult;
import com.hj.kafkaeventrush.repository.EventJoinRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class EventJoinService {

    private final KafkaTemplate<String, EventJoinMessage> kafkaTemplate;
    private final RedisEventJoinService redisEventJoinService;
    private final EventJoinRepository eventJoinRepository;

    public JoinResult requestJoin(Long userId) {
        if (!redisEventJoinService.checkUserAndMark(userId)) {
            return JoinResult.DUPLICATE;
        }

        if (!redisEventJoinService.incrementAndCheckLimit()) {
            return JoinResult.LIMIT_EXCEEDED;
        }

        kafkaTemplate.send("event.join", new EventJoinMessage(userId, LocalDateTime.now()));
        return JoinResult.SUCCESS;
    }

    @Transactional
    public void saveEvent(EventJoinMessage eventJoinMessage) {
        EventJoin entity = EventJoin.builder()
                .userId(eventJoinMessage.getUserId())
                .requestTime(eventJoinMessage.getRequestTime())
                .build();
        eventJoinRepository.save(entity);
    }
}
