package com.footballay.core.web.common.controller.dev;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class NoFaviconController {

    /**
     * /favicon.ico 요청에 대해 204 No Content 응답을 반환합니다.
     * favicon은 스프링 내부가 아닌 다른 곳에서 제공되어야 합니다.
     * @return 204 No Content
     */
    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        return ResponseEntity.noContent().build();
    }
}

