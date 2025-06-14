package com.footballay.core.web.common.controller;

import com.footballay.core.config.AppEnvironmentVariable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.nio.charset.StandardCharsets;

@RequiredArgsConstructor
@Slf4j
@Controller
public class IndexPageController {

    private final RestTemplate restTemplate;
    private final AppEnvironmentVariable envVar;

    @GetMapping("/")
    public ResponseEntity<String> footballayIndexPage() {
        String uri = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(envVar.getFOOTBALLAY_STATIC_DOMAIN())
                .pathSegment("footballay", "mainpage", "index.html")
                .toUriString();
        String html = restTemplate.getForObject(uri, String.class);
        log.info("footballay main Page");
        return ResponseEntity.ok()
                .contentType(new MediaType(MediaType.TEXT_HTML, StandardCharsets.UTF_8))
                .body(html);
    }

}
