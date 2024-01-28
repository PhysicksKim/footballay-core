package com.gyechunsik.scoreboard.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class InmemoryBoardWebsocketService implements BoardWebsocketService {

    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, LocalDateTime> pongTimes = new ConcurrentHashMap<>();

    @Override
    public void registerSession(String sessionId, WebSocketSession session) {
        sessions.put(sessionId, session);
        recordPongTime(sessionId, LocalDateTime.now());
    }

    @Override
    public void removeSession(String sessionId) {
        try {
            sessions.remove(sessionId);
        } catch (Exception e) {
            log.error("removeSession error : " + e.getMessage());
        } finally {
            pongTimes.remove(sessionId);
        }
    }

    @Override
    public WebSocketSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }

    @Override
    public void recordPongTime(String sessionId, LocalDateTime pongTime) {
        pongTimes.put(sessionId, pongTime);
    }

    @Override
    public Collection<WebSocketSession> getAllSessions() {
        return sessions.values();
    }

    public  ConcurrentHashMap<String, LocalDateTime> getAllPongTimes() {
        return pongTimes;
    }

}