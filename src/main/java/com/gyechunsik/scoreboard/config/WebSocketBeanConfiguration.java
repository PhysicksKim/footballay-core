package com.gyechunsik.scoreboard.config;

import com.gyechunsik.scoreboard.websocket.service.sessionbase.InmemoryWebsocketSessionService;
import com.gyechunsik.scoreboard.websocket.service.sessionbase.WebsocketSessionService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebSocketBeanConfiguration {

    @Bean
    public WebsocketSessionService websocketService() {
        return new InmemoryWebsocketSessionService();
    }
}

