package com.footballay.core.web.admin.football.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;

/**
 * Admin Controller
 *
 * NOTE: Admin SPA 정적 파일 서빙은 Nginx를 통해 처리됩니다.
 * - Local: Vite dev server (http://localhost:5173)
 * - Dev: admin.dev.footballay.com → Nginx → S3/CloudFront
 * - Prod: admin.footballay.com → Nginx → S3/CloudFront
 *
 * 이 컨트롤러는 향후 Admin 관련 서버사이드 엔드포인트가 필요할 경우 사용될 수 있습니다.
 * 현재는 모든 Admin API가 AdminApiSportsController, AdminFixtureAvailableController 등
 * 별도의 REST 컨트롤러에서 처리됩니다.
 */
@Controller
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminController.class);

    // Admin SPA 정적 파일 서빙 로직 제거됨 (2025-11-10)
    // - Nginx가 subdomain 기반으로 정적 파일 직접 서빙
    // - Spring Boot는 순수 API 서버 역할만 수행
}
