package com.footballay.core.web.admin.football.controller;

import com.footballay.core.web.admin.football.service.AdminPageAwsService;
import com.footballay.core.web.admin.football.service.AdminUserRoleService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/")
@PreAuthorize("hasAnyRole(\'ADMIN\', \'STREAMER\')")
public class AdminUserRoleController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AdminUserRoleController.class);
    private final AdminUserRoleService adminUserRoleService;
    private final AdminPageAwsService adminPageAwsService;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole(\'ADMIN\', \'STREAMER\')")
    public ResponseEntity<?> getInfo(Authentication authentication) {
        log.info("AdminUserRoleController getRole called for username={}", authentication == null ? "null" : authentication.getName());
        return ResponseEntity.ok().body(adminUserRoleService.getUserInfo(authentication, "/api/admin/role"));
    }

    /**
     * AWS cloudfront -> Cloudflare 로 이전함에 따라서 deprecated 됐습니다.
     * @param response
     * @return
     */
    @Deprecated(since = "2025-05-18")
    @PostMapping("/cloudfront-cookie")
    public ResponseEntity<?> issueCloudfrontSignedCookie(HttpServletResponse response) {
        log.info("[DEPRECATED] AWS signed 쿠키 재발급. deprecated 되어 쿠키를 발급하지 않고 endpoint 만 남겨둡니다");
        // adminPageAwsService.setCloudFrontSignedCookie(response);
        return ResponseEntity.ok().build();
    }

    public AdminUserRoleController(final AdminUserRoleService adminUserRoleService, final AdminPageAwsService adminPageAwsService) {
        this.adminUserRoleService = adminUserRoleService;
        this.adminPageAwsService = adminPageAwsService;
    }
}
