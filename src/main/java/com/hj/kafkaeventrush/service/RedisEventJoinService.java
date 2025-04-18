package com.hj.kafkaeventrush.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

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
