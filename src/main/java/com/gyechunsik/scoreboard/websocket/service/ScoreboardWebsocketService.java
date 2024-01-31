package com.gyechunsik.scoreboard.websocket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyechunsik.scoreboard.websocket.messages.MessageFromControl;
import com.gyechunsik.scoreboard.websocket.messages.MessageToScoreboard;
import com.gyechunsik.scoreboard.websocket.service.sessionbase.SessionServiceIdEnum;
import com.gyechunsik.scoreboard.websocket.service.sessionbase.WebsocketSessionService;
import com.gyechunsik.scoreboard.websocket.validator.UniformEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreboardWebsocketService {

    private final WebsocketSessionService websocketSessionService;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SCOREBOARD_SERVICE_ID = SessionServiceIdEnum.SCOREBOARD.getServiceId();

    public void toScoreboardFromControl(MessageFromControl fromControl) {
        String type = fromControl.getType();

        switch (type) {
            case "score":
                int teamAScore = (int) fromControl.getData().get("teamA");
                int teamBScore = (int) fromControl.getData().get("teamB");
                sendScoreMessage(teamAScore, teamBScore);
                break;
            case "uniform":
                String teamAUniform = (String) fromControl.getData().get("teamA");
                String teamBUniform = (String) fromControl.getData().get("teamB");
                sendUniformMessage(teamAUniform, teamBUniform);
                break;
            case "injury":
                int givenInjuryTime = (int) fromControl.getData().get("given");
                sendGivenInjuryMessage(givenInjuryTime);
                break;
            default:
                log.error("Websocket Message 의 type 이 올바르지 않습니다 :: {}", type);
                break;
        }
    }

    public void sendScoreMessage(int teamAScore, int teamBScore) {
        MessageToScoreboard message = generateScoreMessage(teamAScore, teamBScore);

        sendMessageToSessions(message);
        log.info("Score Message Sent :: {} ", message);
    }

    public void sendUniformMessage(String teamAUniform, String teamBUniform) {
        UniformEnum teamAUniformEnum, teamBUniformEnum;
        try {
            teamAUniformEnum = UniformEnum.valueOf(teamAUniform);
            teamBUniformEnum = UniformEnum.valueOf(teamBUniform);
        } catch (IllegalArgumentException e) {
            log.error("UniformEnum 변환 에러 :: {}", e.getMessage());
            // TODO : Websocket 으로 uniform 형식이 올바르지 않다는 에러 메세지 반송 필요
            return;
        }

        MessageToScoreboard message = generateUniformMessage(teamAUniformEnum, teamBUniformEnum);

        sendMessageToSessions(message);
        log.info("Uniform Message Sent :: {} ", message);
    }

    public void sendGivenInjuryMessage(int givenInjuryTime) {
        MessageToScoreboard message = generateGivenInjuryMessage(givenInjuryTime);

        sendMessageToSessions(message);
        log.info("Given Injury Message Sent :: {} ", message);
    }

    private MessageToScoreboard generateScoreMessage(int teamAScore, int teamBScore) {
        String type = "score";
        Map<String, Object> data = Map.of(
                "teamA", teamAScore,
                "teamB", teamBScore
        );
        UUID messageId = UUID.randomUUID();
        Map<String, Object> metadata = generateMetadata(LocalDateTime.now(), messageId);

        return new MessageToScoreboard(type, data, metadata);
    }

    private MessageToScoreboard generateUniformMessage(UniformEnum teamAUniform, UniformEnum teamBUniform) {
        String type = "uniform";
        Map<String, Object> data = Map.of(
                "teamA", teamAUniform,
                "teamB", teamBUniform
        );
        UUID messageId = UUID.randomUUID();
        Map<String, Object> metadata = generateMetadata(LocalDateTime.now(), messageId);

        return new MessageToScoreboard(type, data, metadata);
    }

    private MessageToScoreboard generateGivenInjuryMessage(int givenInjuryTime) {
        String type = "givenInjuryTime";
        Map<String, Object> data = Map.of(
                "given", givenInjuryTime
        );
        UUID messageId = UUID.randomUUID();
        Map<String, Object> metadata = generateMetadata(LocalDateTime.now(), messageId);

        return new MessageToScoreboard(type, data, metadata);
    }

    private Map<String, Object> generateMetadata(LocalDateTime now, UUID messageId) {
        String formattedDate = now.format(DateTimeFormatter.ISO_DATE_TIME);
        return Map.of(
                "timestamp", formattedDate,
                "messageId", messageId.toString()
        );
    }

    private void sendMessageToSessions(MessageToScoreboard message) {
        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            log.error("JSON 변환 에러 :: {}", e.getMessage());
            // TODO : Websocket 으로 message 형식이 올바르지 않다는 에러 메세지 반송 필요
            return;
        }

        // TODO : 현재 모든 세션에 메세지 전송하는데, 차후 세션 묶음 분리 방안 필요
        // 나중에 코드 부여해서 세션 그룹들을 나눌거임
        websocketSessionService.getAllSessions(SCOREBOARD_SERVICE_ID)
                .forEach(session -> {
                    try {
                        session.sendMessage(new TextMessage(jsonMessage));
                    } catch (IOException e) {
                        log.error("메시지 전송 에러 :: {}", e.getMessage());
                    }
                });
    }
}
