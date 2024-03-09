package com.gyechunsik.scoreboard.websocket.controller;

import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.ScoreBoardRemote;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.AutoRemote;
import com.gyechunsik.scoreboard.websocket.request.AutoRemoteReconnectRequestMessage;
import com.gyechunsik.scoreboard.websocket.request.RemoteConnectRequestMessage;
import com.gyechunsik.scoreboard.websocket.request.RemoteIssueRequestMessage;
import com.gyechunsik.scoreboard.websocket.response.ErrorResponse;
import com.gyechunsik.scoreboard.websocket.response.RemoteConnectResponse;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.RemoteCode;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.service.RemoteCodeMapper;
import com.gyechunsik.scoreboard.websocket.response.RemoteMembersResponse;
import io.jsonwebtoken.lang.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
@Controller
public class RemoteStompController {

    private final ScoreBoardRemote scoreBoardRemote;
    private final AutoRemote autoRemote;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/remote.issuecode")
    @SendToUser("/topic/remote")
    public RemoteConnectResponse issueCode(
            RemoteIssueRequestMessage message,
            Principal principal,
            StompHeaderAccessor headerAccessor
    ) {
        log.info("principal : {}", principal);
        if (principal == null) {
            throw new IllegalArgumentException("general:유저 이름 객체가 비어있습니다. 서버 관리자에게 문의해주세요");
        }
        log.info("message : {}", message);
        log.info("nickname : {}", message.getNickname());
        log.info("is autoRemote : {}", message.isAutoRemote());
        RemoteCode remoteCode = scoreBoardRemote.issueCode(principal, message.getNickname());
        // is need to make AutoRemoteGroup 'NEWLY'?
        if (message.isAutoRemote()) {
            log.info("CALL autoRemote.JoinNewlyFormedAutoGroup() FROM StompController :: {} , {}", remoteCode, principal.getName());
            UUID uuid = autoRemote.joinNewlyFormedAutoGroup(remoteCode, principal);
            autoRemote.cacheUserPrincipalAndUuidForAutoRemote(principal, uuid.toString());
        }

        sendRemoteMembersUpdateMessage(remoteCode);

        log.info("issued remoteCode: {} , user : {}", remoteCode, principal.getName());
        headerAccessor.getSessionAttributes().put("remoteCode", remoteCode.getRemoteCode());
        return new RemoteConnectResponse(remoteCode.getRemoteCode(), message.isAutoRemote());
    }

    /**
     * 원격 client 가 전달받은 code 를 등록합니다.
     * client 는 응답으로 pubPath 를 받아서 해당 주소를 subscribe 합니다.
     * @param message   remoteCode 가 담긴 STOMP 메시지
     * @param principal 원격 컨트롤 요청 client
     * @return pubPath, subPath 를 담은 응답 메세지
     * @since v1.0.0
     */
    @MessageMapping("/remote.connect")
    @SendToUser("/topic/remote")
    public RemoteConnectResponse remoteConnect(
            RemoteConnectRequestMessage message,
            Principal principal,
            StompHeaderAccessor headerAccessor
    ) {
        if (principal == null) {
            throw new IllegalArgumentException("general:코드 등록 에러. 유저이름 객체가 비어있습니다. 서버 관리자에게 문의해주세요.");
        }
        if (!StringUtils.hasText(message.getRemoteCode())) {
            throw new IllegalArgumentException("remotecode:코드 등록 에러. 코드가 비어있습니다.");
        }
        Map<String, Object> sessionAttributes = headerAccessor.getSessionAttributes();
        log.info("attributes : {}", sessionAttributes);

        RemoteCode remoteCode = RemoteCodeMapper.from(message);
        String nickname = message.getNickname().trim();
        scoreBoardRemote.subscribeRemoteCode(remoteCode, principal, nickname);

        log.info("nickname : {}", message.getNickname());
        log.info("is autoRemote : {}", message.isAutoRemote());
        if(message.isAutoRemote()){
            log.info("CALL autoRemote.JoinNewlyFormedAutoGroup() FROM StompController :: {} , {}", remoteCode, principal.getName());
            UUID uuid = autoRemote.joinNewlyFormedAutoGroup(remoteCode, principal);
            autoRemote.cacheUserPrincipalAndUuidForAutoRemote(principal, uuid.toString());
        }

        sendRemoteMembersUpdateMessage(remoteCode);

        sessionAttributes.put("remoteCode", remoteCode.getRemoteCode());
        return new RemoteConnectResponse(remoteCode.getRemoteCode(), message.isAutoRemote());
    }

    /**
     * @param principal
     * @param message
     * @return
     */
    @MessageMapping("/remote.autoreconnect")
    @SendToUser("/topic/remote")
    public RemoteConnectResponse autoRemoteReconnect(
            Principal principal,
            AutoRemoteReconnectRequestMessage message,
            StompHeaderAccessor headerAccessor
    ) {
        if (principal == null) {
            throw new IllegalArgumentException("nickname:유저 이름 객체가 비어있습니다. 서버 관리자에게 문의해주세요");
        }
        String nickname = message.getNickname();
        log.info("message : {}", message);
        log.info("nickname : {}", message.getNickname());
        if(!Strings.hasText(nickname)) {
            throw new IllegalArgumentException("nickname:유저 닉네임이 비어있습니다.");
        }

        RemoteConnectResponse response = scoreBoardRemote.autoRemoteReconnect(principal, nickname);
        log.info("autoRemoteReconnect Response : {}", response);

        sendRemoteMembersUpdateMessage(RemoteCode.of(response.getRemoteCode()));

        headerAccessor.getSessionAttributes().put("remoteCode", response.getRemoteCode());
        return response;
    }

    /**
     * 원격 명령을 중개해줍니다.
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
            throw new IllegalArgumentException("nickname:유저 이름 객체가 비어있습니다. 서버 관리자에게 문의해주세요");
        }

        log.info("remoteCode : {}", remoteCode);
        log.info("remote control message : {}", message);
        log.info("principal name : {}", principal.getName());
        if (!scoreBoardRemote.isValidCode(RemoteCode.of(remoteCode))) {
            throw new IllegalArgumentException("remotecode:유효하지 않은 코드입니다.");
        }

        String sessionRemoteCode = (String) headerAccessor.getSessionAttributes().get("remoteCode");
        log.info("sessionRemoteCode : {}", sessionRemoteCode);
        if (sessionRemoteCode == null || !sessionRemoteCode.equals(remoteCode)) {
            throw new IllegalArgumentException("remotecode:세션에 등록된 코드와 요청된 코드가 일치하지 않습니다. 재접속해주세요.");
        }

        LocalDateTime now = LocalDateTime.now();
        message.put("serverTime", now);
        log.info("server time added To Message : {}", now);

        Consumer<String> sendMessageToUser = (userName) -> {
            messagingTemplate.convertAndSendToUser(userName, "/topic/remote/" + remoteCode, message);
        };
        scoreBoardRemote.sendMessageToSubscribers(remoteCode, principal, sendMessageToUser);
    }

    @MessageMapping("/remote/{remoteCode}/members")
    public void sendRemoteCodeMembers(
            @DestinationVariable("remoteCode") String remoteCode,
            Principal principal
    ) {
        RemoteCode code = RemoteCode.of(remoteCode);
        if(!scoreBoardRemote.isValidCode(code)) {
            throw new IllegalArgumentException("general:유효하지 않은 원격 코드입니다");
        }

        List<String> remoteMembers = scoreBoardRemote.getRemoteMembers(code);
        messagingTemplate.convertAndSendToUser(
                principal.getName(),
                "/topic/remote/"+remoteCode,
                new RemoteMembersResponse(remoteMembers)
        );
        log.info("remotecode=[{}] 채널의 멤버들 : {}", remoteCode, remoteMembers);
        log.info("principal name : {}", principal.getName());
    }

    @MessageExceptionHandler(IllegalArgumentException.class)
    @SendToUser("/topic/remote")
    public ErrorResponse handleException(Exception e) {
        String errMessage = e.getMessage();
        log.error("error : {}", errMessage);
        if(!isErrorFieldRecord(errMessage)) {
            errMessage = "general:" + errMessage;
        }
        return new ErrorResponse(errMessage);
    }

    private boolean isErrorFieldRecord(String message) {
        String[] prefixes = {"general:", "remotecode:", "nickname:"};
        for (String prefix : prefixes) {
            boolean isErrorFieldRecorded = message.startsWith(prefix);
            if(isErrorFieldRecorded) return true;
        }
        return false;
    }

    private void sendRemoteMembersUpdateMessage(RemoteCode remoteCode) {
        List<List<String>> remoteUserDetails = scoreBoardRemote.getRemoteUserDetails(remoteCode);
        List<String> principals = remoteUserDetails.get(0);
        List<String> nicknames = remoteUserDetails.get(1);
        log.info("principals : {}", principals);
        log.info("nicknames : {}", nicknames);
        RemoteMembersResponse memberResponse = new RemoteMembersResponse(nicknames);
        for (String userName : principals) {
            messagingTemplate.convertAndSendToUser(
                    userName,
                    "/topic/remote/"+ remoteCode.getRemoteCode(),
                    memberResponse
            );
        }
    }
}
