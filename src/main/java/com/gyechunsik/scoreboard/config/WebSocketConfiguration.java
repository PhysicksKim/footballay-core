package com.gyechunsik.scoreboard.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final RemoteControlHandler remoteControlHandler;
    private final ScoreBoardWebsocketHandler scoreBoardWebsocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(scoreBoardWebsocketHandler, "/ws-scoreboard")
                .setAllowedOrigins("*");
        registry.addHandler(remoteControlHandler, "/ws-remote")
                .setAllowedOrigins("*");
    }
}
