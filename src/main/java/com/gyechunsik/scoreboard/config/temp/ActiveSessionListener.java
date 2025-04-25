package com.gyechunsik.scoreboard.config.temp;

import jakarta.servlet.http.HttpSessionEvent;
import jakarta.servlet.http.HttpSessionListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ActiveSessionListener implements HttpSessionListener {

    private final AtomicInteger activeSessions = new AtomicInteger(0);

    public int getActiveSessions() {
        return activeSessions.get();
    }

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        activeSessions.incrementAndGet();
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        activeSessions.decrementAndGet();
    }
}