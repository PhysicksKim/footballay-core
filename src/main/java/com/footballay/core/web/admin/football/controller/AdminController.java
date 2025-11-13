package com.footballay.core.web.admin.football.controller;

import com.footballay.core.config.AppEnvironmentVariable;
import com.footballay.core.web.admin.football.service.AdminPageAwsService;
import com.footballay.core.web.admin.football.service.AdminPageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

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

    private final AdminPageService adminPageService;
    private final RestTemplate restTemplate;
    private final String activeProfile;

    @GetMapping({"/admin", "/admin/"})
    public ResponseEntity<String> adminIndexPage(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response) {
        String adminPageUri = adminPageService.getAdminPageUri();
        String html = restTemplate.getForObject(adminPageUri, String.class);
        return ResponseEntity.ok().body(html);
    }

    public AdminController(
            AdminPageService adminPageService,
            RestTemplate restTemplate,
            @Value("${spring.profiles.active:}") String activeProfile) {
        this.adminPageService = adminPageService;
        this.restTemplate = restTemplate;
        this.activeProfile = activeProfile;
    }
}
