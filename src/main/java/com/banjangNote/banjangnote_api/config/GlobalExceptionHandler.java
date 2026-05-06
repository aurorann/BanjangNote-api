package com.banjangNote.banjangnote_api.config;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 모든 컨트롤러에서 발생하는 에러를 감시하고 가로채는 역할을 합니다.
@RestControllerAdvice
public class GlobalExceptionHandler {

    // RuntimeException이 터지면 무조건 이 메서드가 실행됩니다.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        // HTTP 상태코드 400(Bad Request)과 함께 에러 메시지만 프론트엔드로 쏙 보냅니다.
        return ResponseEntity.badRequest().body(e.getMessage());
    }
}
