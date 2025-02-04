package com.gyechunsik.scoreboard.web.common.controller;

import com.gyechunsik.scoreboard.config.CustomEnvironmentVariable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Slf4j
@Controller
@RequiredArgsConstructor
public class IndexPageController {

    private final RestTemplate restTemplate;

    private final CustomEnvironmentVariable envVar;

    @GetMapping("/")
    public ResponseEntity<String> scoreboardIndexPage() {
        String path = "https://static."+envVar.getMainDomain()+"/indexpage/index.html";
        String html = restTemplate.getForObject(path, String.class);
        log.info("main Page");
        return ResponseEntity.ok()
                .contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
                .body(html);
    }

}
