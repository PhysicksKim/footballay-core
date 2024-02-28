package com.gyechunsik.scoreboard.websocket.handler;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompDisconnectInterceptor implements ChannelInterceptor {

    private final SimpMessagingTemplate messagingTemplate;

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        log.info("ChannelInterceptor :: postSend invoked");
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        HttpSession webSession = (HttpSession) sessionAttributes.get("webSession");
        log.info("destination : {}", destination);
        log.info("webSession : {}", webSession);

        if(accessor.getCommand() == null)
            return;

        switch ((accessor.getCommand())) {
            case CONNECT:
                log.info("세션 연결됨 => {}", sessionId);
                break;
            case DISCONNECT:
                log.info("세션 끊음 => {}", sessionId);
                break;
            case SUBSCRIBE:
                log.info("구독 StompSession => {}", sessionId);
                log.info("구독 WebSession => {}", sessionId);
                log.info("구독 주소 => {}", destination);
                messagingTemplate.convertAndSendToUser(webSession.getId(), "/topic/remote.connect", "구독 완료");
                break;
            default:
                log.info("세션 상태 변경 command {} => {}", accessor.getCommand(), sessionId);
                break;
        }
    }
}
