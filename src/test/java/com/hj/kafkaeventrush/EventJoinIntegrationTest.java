package com.hj.kafkaeventrush;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hj.kafkaeventrush.dto.JoinRequest;
import com.hj.kafkaeventrush.repository.EventJoinRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class EventJoinIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EventJoinRepository eventJoinRepository;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    private static final int MAX_JOIN_COUNT = 100;
    private static final int REQUEST_COUNT = 1000;

    @BeforeEach
    void setUp() {
        // Redis 초기화
        redisTemplate.delete("event:join:users");
        redisTemplate.delete("event:join:counter");
        // Event_join 초기화
        eventJoinRepository.deleteAll();
    }

    @Test
    void 동시_요청_1000건_중_100건만_성공_저장된다() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(50);
        CountDownLatch latch = new CountDownLatch(REQUEST_COUNT);

        ObjectMapper objectMapper = new ObjectMapper();

        for (int i = 0; i < REQUEST_COUNT; i++) {
            long userId = i;
            executor.submit(() -> {
                try {
                    JoinRequest request = new JoinRequest(userId);
                    HttpHeaders headers = new HttpHeaders();
                    headers.setContentType(MediaType.APPLICATION_JSON);

                    HttpEntity<String> httpEntity = new HttpEntity<>(
                            objectMapper.writeValueAsString(request),
                            headers
                    );

                    restTemplate.postForEntity("/api/event/join", httpEntity, String.class);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(); // 모든 요청 종료 대기
        Thread.sleep(5000); // Kafka Consumer 처리 대기

        long savedCount = eventJoinRepository.count();
        System.out.println("DB에 저장된 건수: " + savedCount);

        assertThat(savedCount).isEqualTo(MAX_JOIN_COUNT);
    }
}

