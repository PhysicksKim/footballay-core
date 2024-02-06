package com.gyechunsik.scoreboard.websocket.controller;

import com.gyechunsik.scoreboard.websocket.service.Code;
import com.gyechunsik.scoreboard.websocket.service.CodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ScoreBoardStompController {

    private final SimpMessagingTemplate template;
    private final CodeService codeService;

    @MessageMapping("/hello")
    @SendTo("/topic/hello")
    public HelloObj hello(
            Principal principal
    ) {
        log.info("principal : {}", principal);
        log.info("hello");
        return new HelloObj("hello hi " + principal.getName());
    }

    // /app/code.issue
    @MessageMapping("/board/code.issue")
    @SendToUser("/topic/board/code.receive") //  /user/topic/code.receive
    public Code issueCode(
            Principal principal
    ) {
        log.info("principal : {}", principal);
        if(principal == null) {
            throw new IllegalArgumentException("유저이름 객체가 비어있습니다. 서버 관리자에게 문의해주세요");
        }

        Code code = codeService.generateCode();
        log.info("issued code: {} , user : {}", code, principal.getName());
        return code;
    }

    @MessageMapping("/board/code/{codeValue}")
    public void receiveCodeMessage(@DestinationVariable("codeValue") String codeValue) {
        log.info("codeValue : {}", codeValue);
    }

    @MessageExceptionHandler
    public ResponseEntity<?> handleException(Exception e) {
        Map<String, Object> errBody = Map.of("message", e.getMessage());
        return ResponseEntity.badRequest().body(errBody);
    }
}

/*
1) template 으로 보낸 메세지 : principal 로 인식 된다
destination:/user/topic/code
content-type:text/plain;charset=UTF-8
subscription:sub-1
message-id:6674bbdf-4393-3597-b674-c8458675eb30-0
content-length:9

test-code
------
2) @SendToUser 로 보낸 메세지 : 알아서 자체적으로 처리한다
destination:/user/topic/code
content-type:application/json
subscription:sub-1
message-id:6674bbdf-4393-3597-b674-c8458675eb30-1
content-length:24

{"code":"1234test-code"}
 */
