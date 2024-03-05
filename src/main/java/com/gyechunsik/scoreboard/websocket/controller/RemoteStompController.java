package com.gyechunsik.scoreboard.websocket.controller;

import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.ScoreBoardRemote;
import com.gyechunsik.scoreboard.websocket.request.RemoteConnectRequestMessage;
import com.gyechunsik.scoreboard.websocket.request.RemoteIssueRequestMessage;
import com.gyechunsik.scoreboard.websocket.response.CodeIssueResponse;
import com.gyechunsik.scoreboard.websocket.response.ErrorResponse;
import com.gyechunsik.scoreboard.websocket.response.RemoteConnectResponse;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.RemoteCode;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.service.RemoteCodeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Consumer;

// TODO : issueCode, remoteConnect 에서 자동 연결 사용하는지 체크 여부를 받도록 함.
@Slf4j
@RequiredArgsConstructor
@Controller
public class RemoteStompController {

    private final ScoreBoardRemote scoreBoardRemote;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/remote.issuecode")
    @SendToUser("/topic/remote.receivecode")
    public CodeIssueResponse issueCode(
            RemoteIssueRequestMessage message,
            Principal principal,
            StompHeaderAccessor headerAccessor
    ) {
        log.info("principal : {}", principal);
        if (principal == null) {
            throw new IllegalArgumentException("유저 이름 객체가 비어있습니다. 서버 관리자에게 문의해주세요");
        }

        RemoteCode remoteCode = scoreBoardRemote.issueCode(principal, message.getNickname());
        if(message.isAutoRemote()) {
            // is need to make AutoRemoteGroup?

            scoreBoardRemote.subscribeRemoteCode(remoteCode, principal, message.getNickname());
        }


        log.info("issued remoteCode: {} , user : {}", remoteCode, principal.getName());
        headerAccessor.getSessionAttributes().put("remoteCode", remoteCode.getRemoteCode());
        return new CodeIssueResponse(remoteCode.getRemoteCode());
    }

    /**
     * 원격 client 가 전달받은 code 를 등록합니다.
     * client 는 응답으로 pubPath 를 받아서 해당 주소를 subscribe 합니다.
     *
     * @param message   remoteCode 가 담긴 STOMP 메시지
     * @param principal 원격 컨트롤 요청 client
     * @return pubPath, subPath 를 담은 응답 메세지
     * @since v1.0.0
     */
    @MessageMapping("/remote.connect") // /app/remote.connect
    @SendToUser("/topic/remote.connect")
    public RemoteConnectResponse remoteConnect( // todo : return type 에 응답 만들어줘야함
                RemoteConnectRequestMessage message,
                Principal principal,
                StompHeaderAccessor headerAccessor
    ) {
        if (principal == null) {
            throw new IllegalArgumentException("코드 등록 에러. 유저이름 객체가 비어있습니다. 서버 관리자에게 문의해주세요.");
        }
        if (!StringUtils.hasText(message.getRemoteCode())) {
            throw new IllegalArgumentException("코드 등록 에러. 코드가 비어있습니다.");
        }

        RemoteCode remoteCode = RemoteCodeMapper.from(message);
        String nickname = message.getNickname().trim();
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        Object sessionCodeObj = sessionAttributes.get("remoteCode");
        if (sessionCodeObj != null) {
            throw new IllegalArgumentException("이미 등록된 코드가 있습니다. 서버 관리자에게 문의해주세요.");
        }
        log.info("attributes : {}", sessionAttributes);

        scoreBoardRemote.subscribeRemoteCode(remoteCode, principal, nickname);

        sessionAttributes.put("remoteCode", remoteCode.getRemoteCode());
        return new RemoteConnectResponse(remoteCode.getRemoteCode());
    }

    // TODO : 사용자는
    // @MessageMapping("/remote.autoconnect")
    // @SendToUser("/topic/remote.autoconnect")
    // public RemoteConnectResponse remoteAutoConnect(
    //         Principal principal,
    //         RemoteConnectRequestMessage message,
    //         StompHeaderAccessor headerAccessor
    // ) {
    //
    //     return new RemoteConnectResponse(remoteCode.getRemoteCode());
    // }

    /**
     * 원격 명령을 중개해줍니다.
     *
     * @param remoteCode
     * @param message
     * @param principal
     * @param headerAccessor
     * @return
     */
    @MessageMapping("/remote/{remoteCode}")
    public void remoteControl(
            @DestinationVariable("remoteCode") String remoteCode,
            Map<String, Object> message,
            Principal principal,
            StompHeaderAccessor headerAccessor
    ) {
        if (principal == null || !StringUtils.hasText(principal.getName())) {
            throw new IllegalArgumentException("유저 이름 객체가 비어있습니다. 서버 관리자에게 문의해주세요");
        }

        log.info("remoteCode : {}", remoteCode);
        log.info("remote control message : {}", message);
        log.info("principal name : {}", principal.getName());

        if (!scoreBoardRemote.isValidCode(RemoteCode.of(remoteCode))) {
            throw new IllegalArgumentException("유효하지 않은 코드입니다.");
        }

        String sessionRemoteCode = (String) headerAccessor.getSessionAttributes().get("remoteCode");
        log.info("sessionRemoteCode : {}", sessionRemoteCode);
        if (sessionRemoteCode == null || !sessionRemoteCode.equals(remoteCode)) {
            throw new IllegalArgumentException("세션에 등록된 코드와 요청된 코드가 일치하지 않습니다. 재접속해주세요.");
        }

        LocalDateTime now = LocalDateTime.now();
        message.put("serverTime", now);
        log.info("server time added To Message : {}", now);

        Consumer<String> sendMessageToUser = (userName) -> {
            messagingTemplate.convertAndSendToUser(userName,"/topic/remote/" + remoteCode, message);
        };
        scoreBoardRemote.sendMessageToSubscribers(remoteCode, principal, sendMessageToUser);
    }

    @MessageExceptionHandler(IllegalArgumentException.class)
    @SendToUser("/topic/remote/error")
    public ResponseEntity<?> handleException(Exception e) {
        log.error("error : {}", e.getMessage());
        return ResponseEntity.badRequest().body(new ErrorResponse(400, e.getMessage()));
    }

}
