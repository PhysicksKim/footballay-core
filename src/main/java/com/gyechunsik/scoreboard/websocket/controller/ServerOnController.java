package com.gyechunsik.scoreboard.websocket.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/server")
public class ServerOnController {

    @GetMapping("/status")
    public String test() {
        return "on";
    }

}
