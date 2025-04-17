package com.hj.kafkaeventrush.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Kafka로 전송할 이벤트 참여 메시지
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventJoinMessage {
    private Long userId;
    private LocalDateTime requestTime;
}
