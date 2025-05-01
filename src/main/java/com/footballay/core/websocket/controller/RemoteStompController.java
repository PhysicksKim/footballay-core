package com.footballay.core.websocket.controller;

import com.footballay.core.websocket.domain.scoreboard.remote.ScoreBoardRemoteServiceImpl;
import com.footballay.core.websocket.domain.scoreboard.remote.code.RemoteCode;
import com.footballay.core.websocket.request.AutoRemoteReconnectRequestMessage;
import com.footballay.core.websocket.request.RemoteConnectRequestMessage;
import com.footballay.core.websocket.request.RemoteIssueRequestMessage;
import com.footballay.core.websocket.response.ErrorResponse;
import com.footballay.core.websocket.response.RemoteConnectResponse;
import com.footballay.core.websocket.response.RemoteMembersResponse;
import com.footballay.core.websocket.response.SubscribeDoneResponse;
import io.jsonwebtoken.lang.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
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

    private final ScoreBoardRemoteServiceImpl scoreBoardRemoteService;
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
        RemoteCode remoteCode = scoreBoardRemoteService.issueCode(principal, message.getNickname());

        // is need to make AutoRemoteGroup 'NEWLY'?
        if (message.isAutoRemote()) {
            UUID uuid = scoreBoardRemoteService.joinNewlyFormedAutoGroup(remoteCode, principal);
            scoreBoardRemoteService.cacheUserPrincipalAndUuidForAutoRemote(principal, uuid.toString());
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

        RemoteCode remoteCode = RemoteCode.of(message.getRemoteCode());
        String nickname = message.getNickname().trim();
        scoreBoardRemoteService.subscribeRemoteCode(remoteCode, principal, nickname);

        log.info("nickname : {}", message.getNickname());
        log.info("is autoRemote : {}", message.isAutoRemote());
        if(message.isAutoRemote()){
            log.info("CALL autoRemote.JoinNewlyFormedAutoGroup() FROM StompController :: {} , {}", remoteCode, principal.getName());
            UUID uuid = scoreBoardRemoteService.joinNewlyFormedAutoGroup(remoteCode, principal);
            scoreBoardRemoteService.cacheUserPrincipalAndUuidForAutoRemote(principal, uuid.toString());
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

        RemoteConnectResponse response = scoreBoardRemoteService.autoRemoteReconnect(principal, nickname);
        log.info("autoRemoteReconnect _FixtureSingle : {}", response);

        sendRemoteMembersUpdateMessage(RemoteCode.of(response.getRemoteCode()));

        headerAccessor.getSessionAttributes().put("remoteCode", response.getRemoteCode());
        return response;
    }

    @MessageMapping("/remote.subcheck")
    @SendToUser("/topic/remote")
    public SubscribeDoneResponse subscribeCheck() {
        return new SubscribeDoneResponse("/user/topic/remote");
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
        if (!scoreBoardRemoteService.isValidCode(RemoteCode.of(remoteCode))) {
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
        scoreBoardRemoteService.sendMessageToSubscribers(remoteCode, principal, sendMessageToUser);
    }

    @MessageMapping("/remote/{remoteCode}/members")
    public void sendRemoteCodeMembers(
            @DestinationVariable("remoteCode") String remoteCode,
            Principal principal
    ) {
        RemoteCode code = RemoteCode.of(remoteCode);
        if(!scoreBoardRemoteService.isValidCode(code)) {
            throw new IllegalArgumentException("general:유효하지 않은 원격 코드입니다");
        }

        List<String> remoteMembers = scoreBoardRemoteService.getRemoteMembers(code);
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
        if(!isErrorFieldRecord(errMessage)) {
            errMessage = "noshow:" + errMessage;
        }
        return new ErrorResponse(errMessage);
    }

    private boolean isErrorFieldRecord(String message) {
        String[] prefixes = {"general:", "remotecode:", "nickname:", "noshow:"};
        for (String prefix : prefixes) {
            boolean isErrorFieldRecorded = message.startsWith(prefix);
            if(isErrorFieldRecorded) return true;
        }
        return false;
    }

    private void sendRemoteMembersUpdateMessage(RemoteCode remoteCode) {
        List<List<String>> remoteUserDetails = scoreBoardRemoteService.getRemoteUserDetails(remoteCode);
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
