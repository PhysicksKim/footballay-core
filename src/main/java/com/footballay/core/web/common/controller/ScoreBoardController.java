package com.footballay.core.web.common.controller;

import com.footballay.core.config.AppEnvironmentVariable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Slf4j
@Controller
@RequestMapping("/scoreboard")
public class ScoreBoardController {

    private final RestTemplate restTemplate;
    private final AppEnvironmentVariable envVar;

    @GetMapping
    public ResponseEntity<String> scoreboardIndexPage() {
        String path = UriComponentsBuilder.newInstance()
                .scheme("https")
                .host(envVar.getFOOTBALLAY_STATIC_DOMAIN())
                .pathSegment("footballay", "scoreboard", "index.html")
                .toUriString();
        String html = restTemplate.getForObject(path, String.class);
        log.info("football scoreboard page called");
        return ResponseEntity.ok().body(html);
    }

}
