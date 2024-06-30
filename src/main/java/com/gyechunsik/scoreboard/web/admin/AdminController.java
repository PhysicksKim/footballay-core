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

import java.nio.charset.StandardCharsets;

@Slf4j
@RequiredArgsConstructor
@Controller
public class AdminController {

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> adminIndexPage(Authentication authentication) {
        log.info("auth details : {}, isAuth : {}, role : {}",
                authentication.getDetails(), authentication.isAuthenticated(), authentication.getAuthorities());
        String path = "https://static.gyechunsik.site/scoreboard/admin/index.html";
        String html = restTemplate.getForObject(path, String.class);
        log.info("admin Page");

        return ResponseEntity.ok()
                .contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
                .body(html);
    }
}
