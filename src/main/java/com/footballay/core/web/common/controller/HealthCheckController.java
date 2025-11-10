package com.footballay.core.web.common.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 전용 Health Check 엔드포인트
 *
 * 목적:
 * - deploy.sh, Dockerfile HEALTHCHECK에서 사용할 경량 헬스체크
 * - 외부 의존성 없이 애플리케이션 기동 상태만 확인
 * - IndexPageController (/)는 static.footballay.com 의존성으로 인해 불안정할 수 있음
 *
 * 엔드포인트:
 * - GET /health: Root 도메인용 (footballay.com) - Docker HEALTHCHECK 사용
 * - GET /api/health: API 서브도메인용 (api.footballay.com)
 *
 * 응답:
 * - HTTP 200 OK: 애플리케이션 정상 동작
 * - {"status": "UP", "timestamp": "2025-11-01T..."}
 */
@RestController
public class HealthCheckController {

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> simpleHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/api/health")
    public ResponseEntity<Map<String, String>> apiHealth() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "UP");
        response.put("timestamp", Instant.now().toString());
        return ResponseEntity.ok(response);
    }
}
