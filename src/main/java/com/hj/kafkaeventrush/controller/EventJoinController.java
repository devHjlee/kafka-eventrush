package com.hj.kafkaeventrush.controller;

import com.hj.kafkaeventrush.dto.JoinRequest;
import com.hj.kafkaeventrush.service.EventJoinService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/event")
public class EventJoinController {

    private final EventJoinService eventJoinService;

    @PostMapping("/join")
    public ResponseEntity<String> joinEvent(@RequestBody JoinRequest joinRequest) {
        eventJoinService.requestJoin(joinRequest.getUserId());
        return ResponseEntity.ok("참여 요청 완료");
    }
}