package com.gyechunsik.scoreboard.controller;

import com.gyechunsik.scoreboard.messages.Greeting;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final SimpMessagingTemplate template;

    // @SendTo("/topic/greetings")
    @MessageMapping("/hello")
    // @ResponseBody  // JSON 형태로 응답을 반환하도록 지정
    public void greeting(String message) {
        template.convertAndSend("/topic/greetings", message); // topic/market/{마켓아이디}를 듣고있는 client에 전송

        log.info("greetings : {}", message);
        // return new Greeting("hello, " + message + "! from server");
    }
}
