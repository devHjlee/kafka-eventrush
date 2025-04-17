package com.hj.kafkaeventrush.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class EventJoinService {

    public void requestJoin(Long userId) {
        System.out.println("[참여요청] userId: " + userId);
    }
}
