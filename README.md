# Kafka EventRush

Kafka + Redis 기반 선착순 이벤트 실습 프로젝트

[GitHub 바로가기](https://github.com/devHjlee/kafka-eventrush)

---

## 개요

Kafka와 Redis를 활용한 실시간 선착순 이벤트 입니다. 대규모 사용자 요청이 동시에 들어오는 상황에서 중복 참여를 방지하고, 정해진 인원 수만 이벤트에 당첨되도록 처리하는 구조를 구현했습니다.

Kafka는 비동기 메시징 큐로 활용되며, Redis는 중복 검사와 선착순 인원 제한에 사용됩니다.

---

## 사용 기술

- Java 17  
- Spring Boot 3.3 
- Kafka 3.6.1 (KRaft 모드)  
- Redis  
- MySQL  
- JUnit5, Spring Boot Test  
- k6 (성능 테스트 도구)  

---

## Kafka 간략 소개

Kafka는 대용량의 데이터를 빠르고 안정적으로 처리할 수 있는 분산 메시징 시스템입니다.

- **Producer**: 메시지를 Kafka로 전송  
- **Topic**: 메시지를 저장하는 공간 (파티션으로 병렬화)  
- **Consumer Group**: 메시지를 읽는 소비자 그룹 (그룹마다 독립적으로 처리)  
- **Offset**: 메시지의 위치 (Consumer가 어디까지 읽었는지를 관리)

---

## 소스 코드

### EventJoinService

```java
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
```

### RedisEventJoinService

```java
@Service
@RequiredArgsConstructor
public class RedisEventJoinService {

    private static final String USER_SET_KEY = "event:join:users";
    private static final String COUNTER_KEY = "event:join:counter";

    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_COUNT = 100;

    /**
     * 중복 사용자 여부 확인 (참여 여부 확인)
     */
    public boolean checkUserAndMark(Long userId) {
        Long added = redisTemplate.opsForSet().add(USER_SET_KEY, String.valueOf(userId));
        return added != null && added > 0; // true면 신규 사용자
    }

    /**
     * 지정된 인원 수를 초과했는지 확인 (INCR 방식)
     */
    public boolean incrementAndCheckLimit() {
        Long currentCount = redisTemplate.opsForValue().increment(COUNTER_KEY);
        return currentCount != null && currentCount <= MAX_COUNT;
    }
}
```

### Kafka Producer,Consumer Config

```java
@Configuration
public class KafkaProducerConfig {

    @Bean
    public ProducerFactory<String, EventJoinMessage> eventJoinProducerFactory() {
        Map<String, Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(config);
    }

    @Bean
    public KafkaTemplate<String, EventJoinMessage> eventJoinKafkaTemplate() {
        return new KafkaTemplate<>(eventJoinProducerFactory());
    }
}

@Configuration
public class KafkaConsumerConfig {

    @Bean
    public ConsumerFactory<String, EventJoinMessage> eventJoinConsumerFactory() {
        JsonDeserializer<EventJoinMessage> valueDeserializer = new JsonDeserializer<>(EventJoinMessage.class);
        valueDeserializer.addTrustedPackages("*");

        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "event-consumer-group");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valueDeserializer);

        return new DefaultKafkaConsumerFactory<>(config, new StringDeserializer(), valueDeserializer);
    }

    @Bean(name = "eventJoinKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, EventJoinMessage> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, EventJoinMessage> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(eventJoinConsumerFactory());
        factory.setConcurrency(3);
        return factory;
    }
}
```

### Kafka Consumer

```java
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
```

---

## 요청 ~ 저장 흐름

```
1) Client POST /event/join {userId:3}
2) Spring Controller
3) EventJoinService
    - Redis 중복 & 인원 체크
4) Kafka Producer️
5) Kafka Topic (event.join)️
6) Kafka Consumer️
7) MySQL (event_join 테이블 저장)
```

---

## Redis 위치별 장단점

### Producer 측 Redis 사용

| 장점 | 단점 |
|------|------|
| - Kafka로 불필요한 메시지 전송 없음<br>- 빠른 사용자 응답 처리 가능 | - Kafka가 메시지 수신하지 못해도 누락 추적 어려움<br>- 로그 기반 복구 어려움 |

### Consumer 측 Redis 사용

| 장점 | 단점 |
|------|------|
| - Kafka를 통해 모든 이벤트 기록이 남아 장애 대응 용이<br>- Retry 및 로그 분석 쉬움 | - Kafka 트래픽 증가 (필요 없는 메시지도 소비)<br>- 실시간 응답 지연 발생 가능 |

---

프로젝트는 Kafka/Redis 기반의 이벤트 처리 구조를 학습하고, 실전 상황에서의 구조적 의사결정을 연습하기 위해 진행하였습니다.