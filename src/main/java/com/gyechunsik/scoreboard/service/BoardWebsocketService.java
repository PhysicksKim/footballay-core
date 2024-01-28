package com.gyechunsik.scoreboard.service;

import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.Collection;

public interface BoardWebsocketService {
    void registerSession(String sessionId, WebSocketSession session);
    void removeSession(String sessionId);
    WebSocketSession getSession(String sessionId);
    void recordPongTime(String sessionId, LocalDateTime pongTime);
    Collection<WebSocketSession> getAllSessions();
}
