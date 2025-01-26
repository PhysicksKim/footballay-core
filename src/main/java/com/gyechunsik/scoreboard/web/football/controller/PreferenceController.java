package com.gyechunsik.scoreboard.web.football.controller;

import com.gyechunsik.scoreboard.web.football.request.PreferenceKeyRequest;
import com.gyechunsik.scoreboard.web.football.service.PreferenceWebService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/football/preferences")
@Controller
public class PreferenceController {

    private final PreferenceWebService preferenceWebService;

    @PostMapping("/validate")
    public ResponseEntity<Void> validateKey(@RequestBody PreferenceKeyRequest request) {
        log.info("validate key: {}", request.preferencekey());
        boolean isValid = preferenceWebService.validateKey(request.preferencekey());
        log.info("key validation result: {}", isValid);
        if (isValid) {
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

}
