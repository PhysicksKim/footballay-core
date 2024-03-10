package com.gyechunsik.scoreboard.websocket.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/scoreboard")
@Controller
public class ScoreBoardController {

    @GetMapping
    public String scoreboardIndexPage() {
        return "/scoreboard/index";
    }

}
