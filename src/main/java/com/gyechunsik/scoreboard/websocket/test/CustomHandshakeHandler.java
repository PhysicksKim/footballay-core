package com.gyechunsik.scoreboard.websocket.test;

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
        log.info("attributes : {}", attributes.toString());
        // TODO : attrubtes 에서 interceptor 에서 추가한 name 얻어올 수 있는거 확인했음
        // TODO : 이제, handshake 전에 http 로 사용자 이름 등록 로직(중복검사 포함)을 추가하면 됨
        if(principal != null) {
            return principal;
        }

        return new StompPrincipal(UUID.randomUUID().toString());
    }
}

/*
public class CustomHandshakeHandler extends DefaultHandshakeHandler {

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
                                      Map<String, Object> attributes) {
        String userName = (String) attributes.get("userName");
        if (userName != null) {
            return new UserPrincipal(userName); // UserPrincipal은 사용자 정의 Principal 구현입니다.
        }
        return super.determineUser(request, wsHandler, attributes);
    }
}
 */