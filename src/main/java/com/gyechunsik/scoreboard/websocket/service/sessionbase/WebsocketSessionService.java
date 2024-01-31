package com.gyechunsik.scoreboard.websocket.service.sessionbase;

import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

public interface WebsocketSessionService {
    void registerSession(String serviceId, String sessionId, WebSocketSession session);
    void removeSession(String serviceId, String sessionId);
    WebSocketSession getSession(String serviceId, String sessionId);
    void recordPongTime(String serviceId, String sessionId, LocalDateTime pongTime);
    Collection<WebSocketSession> getAllSessions(String serviceId);
    ConcurrentHashMap<String, LocalDateTime> getAllPongTimes(String serviceId);
}
