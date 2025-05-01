package com.gyechunsik.scoreboard.web.gyechunhoe.controller;

import com.gyechunsik.scoreboard.config.AppEnvironmentVariable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;

@Slf4j
@Controller
@RequestMapping("/gyechunhoe")
@RequiredArgsConstructor
public class GyeIndexController {

    private final RestTemplate restTemplate;

    private final AppEnvironmentVariable envVar;

    @GetMapping
    public ResponseEntity<String> gyechunhoeIndexPage() {
        String path = "https://static."+envVar.getDomain()+"/indexpage/index.html";
        String html = restTemplate.getForObject(path, String.class);
        log.info("gyechunhoe main Page");
        return ResponseEntity.ok()
                .contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
                .body(html);
    }

}
