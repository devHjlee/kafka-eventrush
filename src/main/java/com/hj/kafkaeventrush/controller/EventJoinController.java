package com.hj.kafkaeventrush.controller;

import com.hj.kafkaeventrush.dto.JoinRequest;
import com.hj.kafkaeventrush.entity.JoinResult;
import com.hj.kafkaeventrush.service.EventJoinService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/event")
public class EventJoinController {

    private final EventJoinService eventJoinService;

    @PostMapping("/join")
    public ResponseEntity<String> joinEvent(@RequestBody JoinRequest joinRequest) {
        JoinResult result = eventJoinService.requestJoin(joinRequest.getUserId());
        return ResponseEntity.status(result.getStatus()).body(result.getMessage());
    }
}