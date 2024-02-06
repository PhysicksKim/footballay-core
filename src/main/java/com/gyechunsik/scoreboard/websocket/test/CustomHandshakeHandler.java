package com.gyechunsik.scoreboard.websocket.test;

import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@Slf4j
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request,
                                      WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        Principal principal = request.getPrincipal();
        if(principal != null) {
            return principal;
        }
        HttpSession session = (HttpSession) attributes.get("webSession");

        return new StompPrincipal(session.getId());
    }
}