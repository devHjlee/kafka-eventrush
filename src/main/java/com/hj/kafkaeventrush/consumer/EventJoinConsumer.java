package com.hj.kafkaeventrush.consumer;

import com.hj.kafkaeventrush.dto.EventJoinMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EventJoinConsumer {

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
    }
}