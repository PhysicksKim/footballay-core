package com.gyechunsik.scoreboard.config;

import com.gyechunsik.scoreboard.websocket.handler.ControlWSHandler;
import com.gyechunsik.scoreboard.websocket.handler.ScoreBoardWSHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfiguration implements WebSocketConfigurer {

    private final ControlWSHandler controlWSHandler;
    private final ScoreBoardWSHandler scoreBoardWSHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(scoreBoardWSHandler, "/ws-scoreboard")
                .setAllowedOrigins("*");
        registry.addHandler(controlWSHandler, "/ws-control")
                .setAllowedOrigins("*");
    }

}
