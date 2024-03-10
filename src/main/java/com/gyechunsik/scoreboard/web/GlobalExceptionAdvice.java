package com.gyechunsik.scoreboard.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionAdvice {

    @ExceptionHandler(value = Exception.class)
    public String handleException(Exception e) {
        // 모든 예외를 처리하고, "error" 페이지로 리다이렉트
        return "redirect:/error";
    }
}