package com.hj.kafkaeventrush;

import com.hj.kafkaeventrush.dto.EventJoinMessage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
public class EventJoinProducerTest {

    @Autowired
    private KafkaTemplate<String, EventJoinMessage> eventJoinKafkaTemplate;

    @Test
    void 유저_멀티스레드전송() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (long i = 0; i < 300; i++) {
            long userId = i;
            executor.submit(() -> {
                EventJoinMessage message = new EventJoinMessage(userId, LocalDateTime.now());
                SendResult<String, EventJoinMessage> result = null;
                try {
                    result = eventJoinKafkaTemplate.send("event.join", message).get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                System.out.printf("userId=%d → partition=%d%n", userId, result.getRecordMetadata().partition());
            });
        }

        // 전송 완료 대기
        //executor.shutdown();
        Thread.sleep(5000);

        System.out.println("100명 유저 메시지 멀티스레드 전송 완료");
    }

    @Test
    void stickyVSroundrobinTest() throws InterruptedException, ExecutionException {
        // KafkaProducerConfig 설정 추가 필요
        // config.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "org.apache.kafka.clients.producer.RoundRobinPartitioner");
        // config.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        for (long i = 0; i < 300; i++) {
            long userId = i;
            EventJoinMessage message = new EventJoinMessage(userId, LocalDateTime.now());
            SendResult<String, EventJoinMessage> result = eventJoinKafkaTemplate.send("event.join", message).get();
            System.out.printf("userId=%d → partition=%d%n", userId, result.getRecordMetadata().partition());
        }

        // 전송 완료 대기
        Thread.sleep(5000);

        System.out.println("stickyVSroundrobinTest 완료");
    }
}
