package com.gyechunsik.scoreboard.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyechunsik.scoreboard.websocket.messages.MessageFromScoreboard;
import com.gyechunsik.scoreboard.websocket.service.sessionbase.WebsocketSessionService;
import com.gyechunsik.scoreboard.websocket.validator.ValidatorManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.time.LocalDateTime;
import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Component
public class ScoreBoardWSHandler extends TextWebSocketHandler {

    private final ValidatorManager validatorManager;
    private final WebsocketSessionService wsService;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SCOREBOARD_SERVICE_ID = "scoreboard";

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Websocket ScoreBoard Connected : " + session);
        wsService.registerSession(SCOREBOARD_SERVICE_ID, session.getId(), session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        wsService.removeSession(SCOREBOARD_SERVICE_ID, session.getId());
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        wsService.recordPongTime(SCOREBOARD_SERVICE_ID, session.getId(), LocalDateTime.now());
    }

    /**
     * ScoreBoard 에서 보내는 메세지를 받아서 처리합니다.
     * ScoreBoard 는 Server 에 현재 상태 메세지, 수신 확인 메세지 등을 보냅니다.
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.debug("ScoreBoard TextMessage from : {} , message : {}", session, message.getPayload());

        String payload = message.getPayload();
        try {
            MessageFromScoreboard messageFromScoreboard = objectMapper.readValue(payload, MessageFromScoreboard.class);
            if(!validatorManager.validate(messageFromScoreboard)) {
                // Send Back Error Message
                session.sendMessage(new TextMessage("Invalid Message"));
                return;
            }

            // TODO : ScoreBoard 메세지 유효한 경우 처리 구현 필요
            handleValidMessage(session, messageFromScoreboard);
        } catch (Exception e) {
            log.error("ScoreBoard Websocket Error : " + e.getMessage());
        }
    }

    public Collection<WebSocketSession> getAllSessions() {
        return wsService.getAllSessions(SCOREBOARD_SERVICE_ID);
    }

    private void handleValidMessage(WebSocketSession session, MessageFromScoreboard messageFromScoreboard) {
    }
}
