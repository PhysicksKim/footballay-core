package com.gyechunsik.scoreboard.web.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

@Slf4j
@RequiredArgsConstructor
@Controller
public class AdminController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/test/template")
    public ResponseEntity<String> cloudFrontTest() {
        String path = "https://static.gyechunsik.site/scoreboard/admin/index.html";
        String html = restTemplate.getForObject(path, String.class);
        log.info("test/template called");

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @GetMapping("/test/redirect")
    public String cloudFrontRedirect() {
        return "redirect:https://static.gyechunsik.site/scoreboard/admin/index.html";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminIndexPage(Authentication authentication) {
        log.info("auth details : {}", authentication.getDetails());
        log.info("auth isAuth : {}", authentication.isAuthenticated());
        log.info("auth role : {}", authentication.getAuthorities());
        log.info("auth toString : {}", authentication);
        return "admin/adminindex";
    }
}
