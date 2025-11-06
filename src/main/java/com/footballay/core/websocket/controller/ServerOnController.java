package com.footballay.core.websocket.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/server")
public class ServerOnController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ServerOnController.class);

    @GetMapping("/status")
    public String test() {
        return "on";
    }
}
