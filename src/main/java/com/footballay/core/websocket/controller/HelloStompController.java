package com.footballay.core.websocket.controller;

import com.footballay.core.websocket.response.HelloResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Slf4j
@Controller
public class HelloStompController {

    @MessageMapping("/hello")
    @SendTo("/topic/hello")
    public HelloResponse hello(
            Principal principal
    ) {
        log.info("principal : {}", principal);
        log.info("stomp principal name : {}", principal.getName());
        log.info("hello");
        return new HelloResponse("hello hi " + principal.getName());
    }
}
