package com.gyechunsik.scoreboard.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.Collection;

// 차후 AWS Redis 사용이 필요해질 경우를 대비하여, 미리 열어둠
// @Component
@RequiredArgsConstructor
public class AWSRedisWebSocketService implements BoardWebsocketService {

    private final RedisTemplate<String, WebSocketSession> redisTemplate;

    @Override
    public void registerSession(String sessionId, WebSocketSession session) {
        redisTemplate.opsForValue().set(sessionId, session);
    }

    @Override
    public void removeSession(String sessionId) {
        redisTemplate.delete(sessionId);
    }

    @Override
    public WebSocketSession getSession(String sessionId) {
        return redisTemplate.opsForValue().get(sessionId);
    }

    @Override
    public void recordPongTime(String sessionId, LocalDateTime pongTime) {

    }

    @Override
    public Collection<WebSocketSession> getAllSessions() {
        // Redis에서 모든 세션을 가져오는 것은 비효율적일 수 있으므로,
        // 이 메소드는 구현하거나, 필요한 경우 다른 접근 방식을 고려해야 합니다.
        throw new UnsupportedOperationException("Not implemented");
    }
}