package com.gyechunsik.scoreboard.config;

import com.gyechunsik.scoreboard.service.InmemoryBoardWebsocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Component
@EnableScheduling
@Slf4j
@RequiredArgsConstructor
public class PingPongScheduler {

    private final InmemoryBoardWebsocketService wsService;

    @Scheduled(fixedRate = 2000)
    public void reportCurrentUsers() {
        Collection<WebSocketSession> sessions = wsService.getAllSessions();
        log.debug("연결된 세션 : {}", sessions.size());
        sessions.forEach(session -> {
            try {
                session.sendMessage(new PingMessage()); // ping pong 은 자동으로 처리돼서 postman 에서 볼 수 없음.
            } catch (IOException e) {
                log.warn("PingMessage 전송 실패 {}",session.getId(), e);
            }
        });
    }

    // TODO : 정상 동작 검증 필요
    @Scheduled(fixedRate = 10000)
    public void removeOldPongTime() {
        ConcurrentHashMap<String, LocalDateTime> allPongTimes = wsService.getAllPongTimes();
        allPongTimes.forEach((wsSessionId, pongTime) -> {
            if (pongTime.isBefore(LocalDateTime.now().minusSeconds(10))) {
                wsService.removeSession(wsSessionId);
                log.debug("PongTime 이 오래된 세션 종료 : id = {} , lastPong = {}", wsSessionId, pongTime);
            }
        });
    }
}
