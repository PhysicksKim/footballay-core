package com.gyechunsik.scoreboard.web.admin.football.controller;

import com.gyechunsik.scoreboard.web.admin.football.service.AdminPageAwsService;
import com.gyechunsik.scoreboard.web.admin.football.service.AdminUserRoleService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/")
@PreAuthorize("hasAnyRole('ADMIN', 'STREAMER')")
public class AdminUserRoleController {

    private final AdminUserRoleService adminUserRoleService;
    private final AdminPageAwsService adminPageAwsService;

    @GetMapping("/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'STREAMER')")
    public ResponseEntity<?> getInfo(Authentication authentication) {
        log.info("AdminUserRoleController getRole called for username={}", authentication == null ? "null" : authentication.getName());
        return ResponseEntity.ok().body(adminUserRoleService.getUserInfo(authentication, "/api/admin/role"));
    }

    @PostMapping("/cloudfront-cookie")
    public ResponseEntity<?> issueCloudfrontSignedCookie(HttpServletResponse response) {
        log.info("쿠키 재발급");
        adminPageAwsService.setCloudFrontSignedCookie(response);
        return ResponseEntity.ok().build();
    }

}
