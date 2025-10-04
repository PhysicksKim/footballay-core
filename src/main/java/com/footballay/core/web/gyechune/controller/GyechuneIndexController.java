package com.footballay.core.web.gyechune.controller;

import com.footballay.core.config.AppEnvironmentVariable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import java.nio.charset.StandardCharsets;

@Controller
@RequestMapping("/gyechune")
public class GyechuneIndexController {

    private static final Logger log = LoggerFactory.getLogger(GyechuneIndexController.class);

    private final RestTemplate restTemplate;
    private final AppEnvironmentVariable envVar;
    private static final String GYE_DOMAIN_PREFIX = "https://static.";
    private static final String GYE_MAIN_PAGE_PATH = "/indexpage/index.html";

    @GetMapping
    public ResponseEntity<String> gyechuneMainPage() {
        String path = GYE_DOMAIN_PREFIX + envVar.getGYECHUNE_DOMAIN() + GYE_MAIN_PAGE_PATH;
        String html = restTemplate.getForObject(path, String.class);
        log.info("gyechune main Page");
        return ResponseEntity.ok().contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8)).body(html);
    }

    public GyechuneIndexController(final RestTemplate restTemplate, final AppEnvironmentVariable envVar) {
        this.restTemplate = restTemplate;
        this.envVar = envVar;
    }

}
