package com.gyechunsik.scoreboard.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import org.springframework.web.cors.CorsUtils;

import java.io.IOException;

@Slf4j
@Order(1) // 높은 우선순위 설정
@Component
public class CustomCorsFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // CORS 요청인 경우
        if (CorsUtils.isCorsRequest(request)) {
            log.info("CORS request detected. origin : {}", request.getHeader("Origin"));
            String origin = request.getHeader("Origin");
            if (origin == null) {
                origin = "unknown";
            }

            // CORS 응답 헤더 설정 (WebConfig에서 이미 설정되어 있을 수 있음)
            response.setHeader("Access-Control-Allow-Origin", origin);
            response.setHeader("Vary", "Origin");

            // Preflight 요청인 경우
            if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
                log.info("Preflight CORS request from Origin: {}", origin);
            }
        }

        filterChain.doFilter(request, response);
    }
}
