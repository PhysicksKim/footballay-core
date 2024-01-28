package com.gyechunsik.scoreboard.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyechunsik.scoreboard.service.InmemoryBoardWebsocketService;
import com.gyechunsik.scoreboard.websocket.messages.ScoreBoardActionMessage;
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
public class ScoreBoardWebsocketHandler extends TextWebSocketHandler {

    private final InmemoryBoardWebsocketService wsService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        wsService.registerSession(session.getId(), session);
        log.info("ScoreBoard Connected : " + session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("ScoreBoard Disconnected : " + session + " : " + status);
        wsService.removeSession(session.getId());
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        log.debug("ScoreBoard PongMessage from : {} , message : {}", session, message);
        wsService.recordPongTime(session.getId(), LocalDateTime.now());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.debug("ScoreBoard TextMessage from : {} , message : {}", session, message);
        log.debug("message : {}", message.getPayload());
        String payload = message.getPayload();
        try {
            JsonNode jsonNode = objectMapper.readTree(payload);
            String messageType = jsonNode.get("type").asText();

            switch (messageType) {
                case "action":
                    ScoreBoardActionMessage scoreBoardActionMessage = objectMapper.readValue(payload, ScoreBoardActionMessage.class);
                    log.info("websocket action message : {}", scoreBoardActionMessage);
                    break;
                case "error":
                    log.info("websocket action message");
                    break;
                case "setting":
                    log.info("websocket setting message");
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            log.error("ScoreBoard Error : " + e.getMessage());
        }
    }

    public Collection<WebSocketSession> getAllSessions() {
        return wsService.getAllSessions();
    }
}
