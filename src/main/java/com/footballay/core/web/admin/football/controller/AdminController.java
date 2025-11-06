package com.footballay.core.web.admin.football.controller;

import com.footballay.core.config.AppEnvironmentVariable;
import com.footballay.core.web.admin.football.service.AdminPageAwsService;
import com.footballay.core.web.admin.football.service.AdminPageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@Controller
@PreAuthorize("hasRole(\'ADMIN\')")
public class AdminController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminController.class);
    private final AdminPageAwsService adminPageAwsService;
    private final AdminPageService adminPageService;
    private final RestTemplate restTemplate;
    private final AppEnvironmentVariable envVar;
    @Value("${spring.profiles.active:}")
    private String activeProfile;

    @GetMapping("/admin")
    public ResponseEntity<String> adminIndexPage(Authentication authentication, HttpServletRequest request, HttpServletResponse response) {
        String adminPageUri = adminPageService.getAdminPageUri();
        String html = restTemplate.getForObject(adminPageUri, String.class);
        return ResponseEntity.ok().body(html);
    }

    // Admin 정적 파일 프록싱
    @GetMapping("/admin/**")
    @CrossOrigin(origins = "https://localhost:8083", allowCredentials = "true")
    public ResponseEntity<byte[]> serveStaticFile(HttpServletRequest request) {
        boolean isDev = activeProfile.contains("dev");
        if (!isDev) {
            return ResponseEntity.status(403).body(null);
        }
        String requestedPath = request.getRequestURI().substring("/admin/".length());
        String signedUrl = adminPageAwsService.generateSignedUrlForUrl(requestedPath);
        log.info("Signed URL for dev static file of admin page. requestPath={}, signedUrl={}", requestedPath, signedUrl);
        try {
            ResponseEntity<byte[]> response = restTemplate.getForEntity(signedUrl, byte[].class);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(response.getHeaders().getContentType());
            headers.setContentLength(response.getBody() != null ? response.getBody().length : 0);
            return ResponseEntity.ok().headers(headers).body(response.getBody());
        } catch (Exception e) {
            log.error("Failed to fetch file from Signed URL: {}", signedUrl, e);
            return ResponseEntity.status(404).body(null);
        }
    }

    private String rewriteStaticFilePathsToLocalhostPaths(String html) {
        final String ADMIN_STATIC_FILE_PATH = "https://static." + envVar.getFOOTBALLAY_DOMAIN() + "/footballay/admin/";
        final String LOCALHOST_STATIC_FILE_PATH = "https://localhost:8083/admin/";
        return html.replaceAll(ADMIN_STATIC_FILE_PATH, LOCALHOST_STATIC_FILE_PATH);
    }

    public AdminController(final AdminPageAwsService adminPageAwsService, final AdminPageService adminPageService, final RestTemplate restTemplate, final AppEnvironmentVariable envVar) {
        this.adminPageAwsService = adminPageAwsService;
        this.adminPageService = adminPageService;
        this.restTemplate = restTemplate;
        this.envVar = envVar;
    }
}
