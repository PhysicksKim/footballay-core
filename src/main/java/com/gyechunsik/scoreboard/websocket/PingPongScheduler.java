package com.gyechunsik.scoreboard.websocket;

import com.gyechunsik.scoreboard.websocket.service.sessionbase.SessionServiceIdEnum;
import com.gyechunsik.scoreboard.websocket.service.sessionbase.WebsocketSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class PingPongScheduler {

    private final WebsocketSessionService wsService;

    @Scheduled(fixedRate = 3000)
    public void reportCurrentUsers() {
        for(SessionServiceIdEnum serviceIdEnum : SessionServiceIdEnum.values()) {
            Collection<WebSocketSession> sessions = wsService.getAllSessions(serviceIdEnum.getServiceId());
            log.debug("{} 에 연결된 세션 : {}", serviceIdEnum.getServiceId(), sessions.size()); // TODO log.debug 로 변경
            sessions.forEach(session -> {
                try {
                    log.debug("PingMessage 전송 : {}", session.getId());
                    session.sendMessage(new PingMessage());
                } catch (IOException e) {
                    log.warn("PingMessage 전송 실패 {}",session.getId(), e);
                }
            });
        }
    }

    // TODO : 정상 동작 검증 필요
    @Scheduled(fixedRate = 1000)
    public void removeOldPongTime() {
        for(SessionServiceIdEnum serviceIdEnum : SessionServiceIdEnum.values()) {
            ConcurrentHashMap<String, LocalDateTime> allPongTimes = wsService.getAllPongTimes(serviceIdEnum.getServiceId());
            allPongTimes.forEach((wsSessionId, pongTime) -> {
                if (pongTime.isBefore(LocalDateTime.now().minusSeconds(10))) {
                    wsService.removeSession(serviceIdEnum.getServiceId(), wsSessionId);
                    log.info("PongTime 이 오래된 세션 종료 : wsSessionId = {} , lastPong = {}", wsSessionId, pongTime);
                }
            });
        }
    }
}
