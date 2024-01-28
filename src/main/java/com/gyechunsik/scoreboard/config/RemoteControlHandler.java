package com.gyechunsik.scoreboard.config;

import com.gyechunsik.scoreboard.service.InmemoryBoardWebsocketService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;

@Slf4j
@Getter
@RequiredArgsConstructor
@Component
public class RemoteControlHandler extends TextWebSocketHandler {

    private final InmemoryBoardWebsocketService inmemoryBoardWebsocketService;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        inmemoryBoardWebsocketService.registerSession(session.getId(), session);
        log.info("RemoteControl Connected : " + session);
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        inmemoryBoardWebsocketService.removeSession(session.getId()); // 세션 제거
        log.info("RemoteControl Disconnected : " + session + " : " + status);
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        inmemoryBoardWebsocketService.recordPongTime(session.getId(), LocalDateTime.now());
    }
}
