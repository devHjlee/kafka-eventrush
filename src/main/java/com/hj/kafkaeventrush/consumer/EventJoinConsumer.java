package com.hj.kafkaeventrush.consumer;

import com.hj.kafkaeventrush.dto.EventJoinMessage;
import com.hj.kafkaeventrush.entity.EventJoin;
import com.hj.kafkaeventrush.repository.EventJoinRepository;
import com.hj.kafkaeventrush.service.EventJoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class EventJoinConsumer {

    private final EventJoinService eventJoinService;

    @KafkaListener(
            topics = "event.join",
            groupId = "event-consumer-group",
            containerFactory = "eventJoinKafkaListenerContainerFactory"
    )
    public void consume(ConsumerRecord<String, EventJoinMessage> record) {
        int partition = record.partition();
        long offset = record.offset();
        EventJoinMessage message = record.value();
        log.info("Partition: {}, Offset: {}, UserId: {}, Time: {}", partition, offset, message.getUserId(), message.getRequestTime());

        eventJoinService.saveEvent(message);
    }
}