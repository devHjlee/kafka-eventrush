package com.hj.kafkaeventrush;

import com.hj.kafkaeventrush.dto.EventJoinMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class EventJoinProducerTest {

    @Autowired
    private KafkaTemplate<String, EventJoinMessage> eventJoinKafkaTemplate;

    @Test
    void 유저_100명_멀티스레드전송() {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (long i = 0; i < 100; i++) {
            long userId = i;
            executor.submit(() -> {
                EventJoinMessage message = new EventJoinMessage(userId, LocalDateTime.now());
                eventJoinKafkaTemplate.send("event.join", message);
                System.out.printf("[Thread: %s] 전송 완료 userId=%d%n", Thread.currentThread().getName(), userId);
            });
        }

        // 전송 완료 대기
        executor.shutdown();

        System.out.println("100명 유저 메시지 멀티스레드 전송 완료");
    }
}