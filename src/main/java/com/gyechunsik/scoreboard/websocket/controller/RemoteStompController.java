package com.gyechunsik.scoreboard.websocket.controller;

import com.gyechunsik.scoreboard.websocket.request.RemoteConnectMessage;
import com.gyechunsik.scoreboard.websocket.response.ErrorResponse;
import com.gyechunsik.scoreboard.websocket.response.RemoteConnectResponse;
import com.gyechunsik.scoreboard.websocket.response.SuccessResponse;
import com.gyechunsik.scoreboard.websocket.service.RemoteCode;
import com.gyechunsik.scoreboard.websocket.service.RemoteCodeMapper;
import com.gyechunsik.scoreboard.websocket.service.RemoteCodeService;
import jakarta.websocket.Session;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.sql.exec.spi.StandardEntityInstanceResolver;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketSession;

import java.security.Principal;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Controller
public class RemoteStompController {

    private final RemoteCodeService remoteCodeService;

    /**
     * 원격 client 가 전달받은 code 를 등록합니다.
     * client 는 응답으로 pubPath 를 받아서 해당 주소를 subscribe 합니다.
     * pubPath 는 "/topic/board.{remoteCode}" 입니다.
     *
     * @param message   remoteCode 가 담긴 STOMP 메시지
     * @param principal 원격 컨트롤 요청 client
     * @return pubPath 를 담은 응답 메세지
     * @since v1.0.0
     */
    @MessageMapping("/remote.connect")
    @SendToUser("/topic/remote.connect")
    public SuccessResponse remoteConnect(
            @Payload RemoteConnectMessage message,
            Principal principal,
            StompHeaderAccessor headerAccessor
    ) {
        log.info("before code map :: {}", remoteCodeService.getCodeSessionMap().toString());
        if (principal == null) {
            throw new IllegalArgumentException("코드 등록 에러. 유저이름 객체가 비어있습니다. 서버 관리자에게 문의해주세요.");
        }

        RemoteCode remoteCode = RemoteCodeMapper.from(message);
        if (!remoteCodeService.enrollPubUser(remoteCode, principal.getName())) {
            throw new IllegalArgumentException("이미 연결된 코드입니다.");
        }

        log.info("RemoteCode Publisher Enrolled :: remote connect = {} , Username = {}",
                remoteCode, principal.getName());

        // subPath = "/topic/board.{remoteCode}";
        log.info("after code map :: {}", remoteCodeService.getCodeSessionMap().toString());
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        if(sessionAttributes == null) {
            throw new IllegalArgumentException("세션 어트리뷰트가 비어있습니다. 서버 관리자에게 문의해주세요.");
        }

        log.info("before attributes : {}", sessionAttributes);
        sessionAttributes.put("remoteCode", remoteCode.getRemoteCode());
        log.info("after attributes : {}", sessionAttributes);
        return new RemoteConnectResponse(200, "원격 등록에 성공했습니다.", remoteCode.getRemoteCode());
    }

    @MessageMapping("/remote/{remoteCode}")
    @SendTo("/topic/board/{remoteCode}")
    public Map<String, String> remoteControl(
            @DestinationVariable("remoteCode") String remoteCode,
            Map<String, String> message,
            Principal principal,
            StompHeaderAccessor headerAccessor
    ) {
        if(principal == null || !StringUtils.hasText(principal.getName())) {
            throw new IllegalArgumentException("유저 이름 객체가 비어있습니다. 서버 관리자에게 문의해주세요");
        }

        log.info("remoteCode : {}", remoteCode);
        log.info("remote control message : {}", message);
        log.info("principal name : {}", principal.getName());

        if(remoteCodeService.isValidCodeAndUser(RemoteCode.of(remoteCode), principal.getName())) {
            // throw new IllegalArgumentException("유효하지 않은 코드입니다. 코드를 다시 확인해주세요.");
            log.info("이미 등록된 사용자의 add 명령 발생"); // TODO : 원격 컨트롤 요청자가 등록 요청을 반복하면 어떻게 처리해야하지?
        }

        String sessionRemoteCode = (String) headerAccessor.getSessionAttributes().get("remoteCode");
        if(sessionRemoteCode == null || !sessionRemoteCode.equals(remoteCode)) {
            throw new IllegalArgumentException("세션에 등록된 코드와 요청된 코드가 일치하지 않습니다.");
        }

        log.info("code = {} 의 메세지 전송 validation 통과", remoteCode);
        return message;
    }


    @MessageExceptionHandler(IllegalArgumentException.class)
    @SendToUser("/topic/error")
    public ResponseEntity<?> handleException(Exception e) {
        log.error("error : {}", e.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
    }
}
