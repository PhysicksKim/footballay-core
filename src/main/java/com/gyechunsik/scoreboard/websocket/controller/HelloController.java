package com.gyechunsik.scoreboard.websocket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HelloController {

    @GetMapping("/hello")
    public String hello() {
        return "hellopage.html";
    }

    @GetMapping("/scoreboard")
    public String scoreboard() {
        return "scoreboard/index.html";
    }
}
