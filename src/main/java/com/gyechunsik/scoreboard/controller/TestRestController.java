package com.gyechunsik.scoreboard.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestRestController {

    @GetMapping("/api/test")
    public String test() {
        return "test";
    }

}
