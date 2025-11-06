package com.footballay.core.websocket.handler;

import com.footballay.core.websocket.user.StompPrincipal;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;
import java.security.Principal;
import java.util.Map;

public class CustomHandshakeHandler extends DefaultHandshakeHandler {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CustomHandshakeHandler.class);

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        Principal principal = request.getPrincipal();
        if (principal != null) {
            return principal;
        }
        HttpSession session = (HttpSession) attributes.get("webSession");
        log.info("웹소켓 principal : {}", session.getId());
        return new StompPrincipal(session.getId());
    }
}
