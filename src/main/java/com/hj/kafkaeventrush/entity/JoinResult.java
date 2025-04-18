package com.hj.kafkaeventrush.entity;

import org.springframework.http.HttpStatus;

public enum JoinResult {
    SUCCESS("이벤트 참여가 완료되었습니다.", HttpStatus.OK),
    DUPLICATE("이미 참여하셨습니다.", HttpStatus.CONFLICT),
    LIMIT_EXCEEDED("선착순이 마감되었습니다.", HttpStatus.TOO_MANY_REQUESTS);

    private final String message;
    private final HttpStatus status;

    JoinResult(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
