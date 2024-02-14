package com.gyechunsik.scoreboard.websocket.user;

import com.gyechunsik.scoreboard.websocket.handler.CustomHandshakeHandler;

import java.security.Principal;

/**
 * <pre>
 * 비회원의 STOMP 연결 시 Principal == null 이 되는 문제를 해결하기 위해,
 * {@link CustomHandshakeHandler} 에서 null 대신 비회원 닉네임이 들어간 Principal 객체를 생성합니다.
 *
 * STOMP 메세지의 개인 메세지 구분은 Principal 객체에 의해서 이뤄집니다.
 * 만약 handshake 시점에 해당 사용자가 비로그인 상태라면, WebSocketSession 의 principal 도 null 이 됩니다.
 * WebSocketSession 의 principal 이 null 이면, STOMP 메세지의 개인 메세지 구분이 불가능합니다.
 * 따라서, 비로그인 상태의 사용자에게도 개인 메세지를 보낼 수 있도록 간단히 Guest Username 을 추가한 Principal 을 사용하도록 합니다.
 *
 * 현재는 SessionId 를 사용합니다.
 * @see {@}com.gyechunsik.scoreboard.websocket.test.CustomHandshakeHandler
 * </pre>
 */
public class StompPrincipal implements Principal {

    private String name;

    public StompPrincipal(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}