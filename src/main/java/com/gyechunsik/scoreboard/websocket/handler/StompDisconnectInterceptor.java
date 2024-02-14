package com.gyechunsik.scoreboard.websocket.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompDisconnectInterceptor implements ChannelInterceptor {

    @Override
    public void postSend(Message<?> message, MessageChannel channel, boolean sent) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        String sessionId = accessor.getSessionId();

        if(accessor.getCommand() == null)
            return;

        switch ((accessor.getCommand())) {
            case CONNECT:
                log.info("세션 연결됨 => {}", sessionId);
                break;
            case DISCONNECT:
                log.info("세션 끊음 => {}", sessionId);
                break;
            default:
                break;
        }
    }
}
