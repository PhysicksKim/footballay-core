package com.gyechunsik.scoreboard.websocket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyechunsik.scoreboard.websocket.messages.MessageFromControl;
import com.gyechunsik.scoreboard.websocket.service.ScoreboardWebsocketService;
import com.gyechunsik.scoreboard.websocket.service.sessionbase.SessionServiceIdEnum;
import com.gyechunsik.scoreboard.websocket.service.sessionbase.WebsocketSessionService;
import com.gyechunsik.scoreboard.websocket.validator.ValidatorManager;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.time.LocalDateTime;

@Slf4j
@Getter
@RequiredArgsConstructor
@Component
public class ControlWSHandler extends TextWebSocketHandler {

    private final WebsocketSessionService wsService;
    private final ValidatorManager validatorManager;
    private final ScoreboardWebsocketService scoreboardService;

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String CONTROL_SERVICE_ID = SessionServiceIdEnum.CONTROL.getServiceId();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("Websocket Control Connected : " + session);
        wsService.registerSession(CONTROL_SERVICE_ID, session.getId(), session);
        super.afterConnectionEstablished(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        wsService.removeSession(CONTROL_SERVICE_ID, session.getId()); // 세션 제거
        log.info("Websocket Control Disconnected : " + session + " : " + status);
    }

    @Override
    protected void handlePongMessage(WebSocketSession session, PongMessage message) throws Exception {
        wsService.recordPongTime(CONTROL_SERVICE_ID, session.getId(), LocalDateTime.now());
    }

    /**
     * Control 에서 보내는 메세지를 받아서
     * @param session
     * @param message
     * @throws Exception
     */
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        log.debug("Control TextMessage from : {} , message : {}", session, message.getPayload());

        String payload = message.getPayload();
        try {
            MessageFromControl messageFromControl = objectMapper.readValue(payload, MessageFromControl.class);

            if(!validatorManager.validate(messageFromControl)) {
                session.sendMessage(new TextMessage("Invalid Message"));
                return;
            }

            handleValidMessage(session, messageFromControl);
        } catch (Exception e) {
            log.error("ScoreBoard Websocket Error : " + e.getMessage());
        }
    }

    private void handleValidMessage(WebSocketSession session, MessageFromControl messageFromControl) {
        try {
            scoreboardService.toScoreboardFromControl(messageFromControl);
        } catch (Exception e) {
            log.error("ScoreBoard Websocket Message Send Error : " + e.getMessage());
            try {
                session.sendMessage(new TextMessage("Invalid Message"));
            } catch (IOException wsSendError) {
                log.error("Control Websocket Error : " + wsSendError.getMessage());
            }
        }
    }
}
