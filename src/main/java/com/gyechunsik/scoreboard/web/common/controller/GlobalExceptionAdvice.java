package com.gyechunsik.scoreboard.web.common.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@ControllerAdvice
@Order(Ordered.LOWEST_PRECEDENCE) // 가장 낮은 우선순위 설정
public class GlobalExceptionAdvice {

    @ExceptionHandler(value = Exception.class)
    public String handleException(Exception e) {
        log.warn("Exception occurred: ", e);
        return "redirect:/error";
    }
}