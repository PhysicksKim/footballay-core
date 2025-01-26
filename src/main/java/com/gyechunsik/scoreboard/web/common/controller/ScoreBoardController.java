package com.gyechunsik.scoreboard.web.common.controller;

import com.gyechunsik.scoreboard.config.CustomEnvironmentVariable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Slf4j
@Controller
@RequestMapping("/scoreboard")
public class ScoreBoardController {

    private final RestTemplate restTemplate = new RestTemplate();
    private final CustomEnvironmentVariable envVar;

    @GetMapping
    public ResponseEntity<String> scoreboardIndexPage() {
        String path = "https://static."+envVar.getMainDomain()+"/scoreboard/index.html";
        String html = restTemplate.getForObject(path, String.class);
        log.info("test/template called");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

}
