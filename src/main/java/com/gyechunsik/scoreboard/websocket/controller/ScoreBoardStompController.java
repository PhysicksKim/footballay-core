package com.gyechunsik.scoreboard.websocket.controller;

import com.gyechunsik.scoreboard.websocket.response.HelloResponse;
import com.gyechunsik.scoreboard.websocket.response.IssuedCodeResponse;
import com.gyechunsik.scoreboard.websocket.service.RemoteCode;
import com.gyechunsik.scoreboard.websocket.service.RemoteCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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
    private final RemoteCodeService remoteCodeService;

    @MessageMapping("/hello")
    @SendTo("/topic/hello")
    public HelloResponse hello(
            Principal principal
    ) {
        log.info("principal : {}", principal);
        log.info("hello");
        return new HelloResponse("hello hi " + principal.getName());
    }

    /**
     * 코드를 발급합니다. 클라이언트에서는 발급받은 코드를 사용하여
     * @param principal
     * @return
     */
    // /app/code.issue
    @MessageMapping("/board/remotecode.issue")
    @SendToUser("/topic/board/remotecode.receive") //  /user/topic/code.receive
    public IssuedCodeResponse issueCode(
            Principal principal
    ) {
        log.info("principal : {}", principal);
        if(principal == null) {
            throw new IllegalArgumentException("유저 이름 객체가 비어있습니다. 서버 관리자에게 문의해주세요");
        }

        RemoteCode remoteCode = remoteCodeService.generateCode();
        log.info("issued remoteCode: {} , user : {}", remoteCode, principal.getName());
        return new IssuedCodeResponse("200", "코드가 발급되었습니다.", remoteCode.getRemoteCode());
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
