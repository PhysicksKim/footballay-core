package com.footballay.core.web.common.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomErrorController.class);

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        // 에러 페이지 경로를 반환
        log.info("error page referer : {}", request.getHeader("referer"));
        return "error";
    }
}
