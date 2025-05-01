package com.footballay.core.websocket.domain.scoreboard.remote.code.service;

import com.footballay.core.websocket.domain.scoreboard.remote.code.RemoteCode;

import java.time.Duration;
import java.util.Map;
import java.util.Set;

public interface RemoteCodeService {

    RemoteCode generateCodeAndSubscribe(String principalName, String nickname);

    /**
     * 해당 코드를 구독하는 client 들의 이름 목록을 반환합니다.
     * 이름은 각 웹소켓 클라이언트의 Principal.getName() 값을 담고 있습니다.
     * @param remoteCode 구독자 목록을 조회할 코드
     * @return 구독자 이름 목록
     */
    Map<Object, Object> getSubscribers(String remoteCode);

    Set<String> getNicknames(String remoteCode);

    void addSubscriber(RemoteCode remoteCode, String subscriber, String nickname);

    /**
     * @param remoteCode
     * @param subscriber
     * @return is Empty
     */
    boolean removeSubscriber(RemoteCode remoteCode, String subscriber);

    void setExpiration(RemoteCode remoteCode, Duration duration);

    void refreshExpiration(RemoteCode remoteCode);

    boolean isValidCode(RemoteCode remoteCode);

    boolean expireCode(RemoteCode remoteCode);
}