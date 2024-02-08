package com.gyechunsik.scoreboard.websocket.controller;

import com.gyechunsik.scoreboard.websocket.response.BoardCodeIssueResponse;
import com.gyechunsik.scoreboard.websocket.service.RemoteCode;
import com.gyechunsik.scoreboard.websocket.service.RemoteCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.Map;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ScoreBoardStompController {

    private final SimpMessagingTemplate template;
    private final RemoteCodeService remoteCodeService;

    /**
     * 코드를 발급합니다. client 에서는 발급받은 코드를 사용하여 원격 명령을 받을 subPath channel 을 구독합니다.
     * @param principal
     * @return
     */
    //  /app/code.issue
    //  /user/topic/code.receive
    @MessageMapping("/board/remotecode.issue")
    @SendToUser("/topic/board/remotecode.receive")
    public BoardCodeIssueResponse issueCode(
            Principal principal
    ) {
        log.info("principal : {}", principal);
        if (principal == null) {
            throw new IllegalArgumentException("유저 이름 객체가 비어있습니다. 서버 관리자에게 문의해주세요");
        }

        RemoteCode remoteCode = remoteCodeService.generateCode();
        log.info("issued remoteCode: {} , user : {}", remoteCode, principal.getName());
        log.info("code map :: {}", remoteCodeService.getCodeSessionMap().toString());
        return new BoardCodeIssueResponse(200, "코드가 발급되었습니다.", remoteCode.getRemoteCode());
    }

    @MessageMapping("/board/remotecode.expire/{remoteCode}")
    @SendToUser("/topic/remotecode.expire")
    public ResponseEntity<?> expireCode(
            @DestinationVariable("remoteCode") RemoteCode remoteCode,
            Principal principal
    ) {
        if (principal == null) {
            throw new IllegalArgumentException("유저 이름 객체가 비어있습니다. 서버 관리자에게 문의해주세요");
        }
        if (remoteCode == null || !StringUtils.hasText(remoteCode.getRemoteCode())) {
            throw new IllegalArgumentException("유효하지 않은 코드입니다. 코드를 다시 확인해주세요.");
        }

        if (remoteCodeService.expireCode(remoteCode)) {
            return ResponseEntity.ok().body(Map.of("code", "200", "message", "코드가 성공적으로 파기되었습니다."));
        } else {
            return ResponseEntity.badRequest().body(Map.of("code","400","message", "코드가 존재하지 않습니다."));
        }
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
