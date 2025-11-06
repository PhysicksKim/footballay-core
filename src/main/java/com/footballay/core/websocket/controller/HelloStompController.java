package com.footballay.core.websocket.controller;

import com.footballay.core.websocket.response.HelloResponse;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import java.security.Principal;

@Controller
public class HelloStompController {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(HelloStompController.class);

    @MessageMapping("/hello")
    @SendTo("/topic/hello")
    public HelloResponse hello(Principal principal) {
        log.info("principal : {}", principal);
        log.info("stomp principal name : {}", principal.getName());
        log.info("hello");
        return new HelloResponse("hello hi " + principal.getName());
    }
}
