package com.gyechunsik.scoreboard.websocket.handler;

import com.gyechunsik.scoreboard.websocket.user.StompPrincipal;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

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
        log.info("웹소켓 principal : {}", session.getId());
        return new StompPrincipal(session.getId());
    }
}