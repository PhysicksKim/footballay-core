package com.gyechunsik.scoreboard.websocket.service.sessionbase;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@RequiredArgsConstructor
public class InmemoryWebsocketSessionService implements WebsocketSessionService {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, WebSocketSession>> serviceSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, LocalDateTime>> servicePongTimes = new ConcurrentHashMap<>();

    @PostConstruct
    private void init() {
        // SessionServiceIdEnum 을 순회하면서 초기 맵 생성
        for (SessionServiceIdEnum serviceIdEnum : SessionServiceIdEnum.values()) {
            String serviceId = serviceIdEnum.getServiceId();
            serviceSessions.put(serviceId, new ConcurrentHashMap<>());
            servicePongTimes.put(serviceId, new ConcurrentHashMap<>());
        }
    }

    @Override
    public void registerSession(String serviceId, String sessionId, WebSocketSession session) {
        serviceSessions.computeIfAbsent(serviceId, k -> new ConcurrentHashMap<>()).put(sessionId, session);
        recordPongTime(serviceId, sessionId, LocalDateTime.now());
    }

    @Override
    public void removeSession(String serviceId, String sessionId) {
        try {
            serviceSessions.computeIfPresent(serviceId, (key, sessionsMap) -> {
                sessionsMap.remove(sessionId);
                return sessionsMap;
            });
        } catch (Exception e) {
            log.error("removeSession error : " + e.getMessage());
        } finally {
            servicePongTimes.computeIfPresent(serviceId, (key, pongMap) -> {
                pongMap.remove(sessionId);
                return pongMap;
            });
        }
    }

    @Override
    public WebSocketSession getSession(String serviceId, String sessionId) {
        ConcurrentHashMap<String, WebSocketSession> sessions = serviceSessions.get(serviceId);
        if(sessions == null) {
            return null;
        }

        return sessions.get(sessionId);
    }

    @Override
    public void recordPongTime(String serviceId, String sessionId, LocalDateTime pongTime) {
        ConcurrentHashMap<String, LocalDateTime> pongs = servicePongTimes.get(serviceId);
        if(pongs == null) {
            throw new IllegalArgumentException("pongTime 이 존재하지 않는 serviceId 입니다.");
        }

        pongs.put(sessionId, pongTime);
    }

    @Override
    public Collection<WebSocketSession> getAllSessions(String serviceId) {
        ConcurrentHashMap<String, WebSocketSession> sessions = serviceSessions.get(serviceId);
        if(sessions == null) {
            throw new IllegalArgumentException("serviceId 가 존재하지 않습니다.");
        }

        return sessions.values();
    }

    @Override
    public ConcurrentHashMap<String, LocalDateTime> getAllPongTimes(String serviceId) {
        ConcurrentHashMap<String, LocalDateTime> pongs = servicePongTimes.get(serviceId);
        if(pongs == null) {
            throw new IllegalArgumentException("serviceId 가 존재하지 않습니다.");
        }

        return pongs;
    }

}