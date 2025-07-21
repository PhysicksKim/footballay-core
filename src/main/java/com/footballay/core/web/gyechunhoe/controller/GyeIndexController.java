package com.footballay.core.web.gyechunhoe.controller;

import com.footballay.core.config.AppEnvironmentVariable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/gyechunhoe")
public class GyeIndexController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GyeIndexController.class);
    private final RestTemplate restTemplate;
    private final AppEnvironmentVariable envVar;
    private static final String GYE_DOMAIN_PREFIX = "https://static.";
    private static final String GYE_MAIN_PAGE_PATH = "/indexpage/index.html";
    private static final String GYE_TEST_MAIN_PAGE_PATH = "/test-main-page/index.html";

    @GetMapping
    public ResponseEntity<String> gyechunhoeMainPage() {
        String path = GYE_DOMAIN_PREFIX + envVar.getGYE_DOMAIN() + GYE_MAIN_PAGE_PATH;
        String html = restTemplate.getForObject(path, String.class);
        log.info("gyechunhoe main Page");
        return ResponseEntity.ok().contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8)).body(html);
    }

    @GetMapping("/hwkemf/test-main-page")
    public ResponseEntity<String> testMainPage() {
        String path = GYE_DOMAIN_PREFIX + envVar.getGYE_DOMAIN() + GYE_TEST_MAIN_PAGE_PATH;
        String html = restTemplate.getForObject(path, String.class);
        log.info("gyechunhoe test main Page");
        return ResponseEntity.ok().contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8)).body(html);
    }

    public GyeIndexController(final RestTemplate restTemplate, final AppEnvironmentVariable envVar) {
        this.restTemplate = restTemplate;
        this.envVar = envVar;
    }
}
