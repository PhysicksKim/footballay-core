package com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote;

import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.AutoRemoteConnect;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.RemoteCode;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.service.RemoteCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * 원격 연결 도메인 Root 에 해당합니다.
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ScoreBoardRemote {

    private final RemoteCodeService remoteCodeService;
    private final AutoRemoteConnect autoRemoteConnect;

    /**
     * 원격 연결을 위한 코드를 발급합니다.
     * @param principal
     * @param nickname
     * @return 발급된 RemoteCode
     */
    public RemoteCode issueCode(Principal principal, String nickname) {
        return remoteCodeService.generateCodeAndSubscribe(principal.getName(), nickname);
    }

    /**
     * 존재하는 원격 코드에 연결합니다.
     * @param remoteCode
     * @param principal
     * @param nickname
     */
    public void subscribeRemoteCode(RemoteCode remoteCode, Principal principal, String nickname) {
        if (!remoteCodeService.isValidCode(remoteCode)) {
            throw new IllegalArgumentException("코드 연결 에러. 유효하지 않은 코드입니다.");
        }
        if (Strings.isEmpty(nickname)) {
            throw new IllegalArgumentException("코드 연결 에러. 닉네임이 비어있습니다.");
        }
        remoteCodeService.addSubscriber(remoteCode, principal.getName(), nickname);
    }


    /**
     * 자동 원격 연결 형성 요청시 UUID 를 발급합니다.
     * @param activeRemoteCode 현재 연결된 Remote Code
     * @return UUID 는 자동 원격 연결을 위한 사용자 식별자로 사용됩니다. 쿠키로 반환합니다.
     */
    public UUID issueUUIDForAnonymousUserCookie(RemoteCode activeRemoteCode) {
        // UUID uuid = autoRemote.rejoinPreviouslyFormedAutoGroup(activeRemoteCode);
        // log.info("Should This UUID to Set Cookie: {}", uuid);
        // return uuid;
        return null;
    }

    public void cacheUserBeforeAutoRemote(Principal principal, String userUUID) {
        autoRemoteConnect.cacheUserBeforeAutoRemote(principal, userUUID);
    }

    public void sendMessageToSubscribers(String remoteCode, Principal remotePublisher, Consumer<String> sendMessageToSubscriber) {
        remoteCodeService.refreshExpiration(RemoteCode.of(remoteCode));
        Map<Object, Object> subscribers = remoteCodeService.getSubscribers(remoteCode);
        subscribers.keySet().forEach(key -> {
            String subscriber = (String) key;
            log.info("subscriber : {}", subscriber);
            if(!subscriber.equals(remotePublisher.getName())) {
                log.info("send to user : {}", subscriber);
                sendMessageToSubscriber.accept(subscriber);
            } else {
                log.info("skip send to user : {}", subscriber);
            }
        });
    }

    public boolean isValidCode(RemoteCode remoteCode) {
        return remoteCodeService.isValidCode(remoteCode);
    }

}
