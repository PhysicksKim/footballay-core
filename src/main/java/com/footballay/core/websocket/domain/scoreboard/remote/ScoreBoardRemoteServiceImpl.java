package com.footballay.core.websocket.domain.scoreboard.remote;

import com.footballay.core.websocket.domain.scoreboard.remote.autoremote.service.AutoRemoteService;
import com.footballay.core.websocket.domain.scoreboard.remote.code.RemoteCode;
import com.footballay.core.websocket.domain.scoreboard.remote.code.service.RemoteCodeService;
import com.footballay.core.websocket.response.RemoteConnectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class ScoreBoardRemoteServiceImpl {

    private final RemoteCodeService remoteCodeService;
    private final AutoRemoteService autoRemoteService;

    /**
     * 자동 연결 과정에서 사용할 Principal - UUID 쌍을 Redis 에 캐싱합니다.
     * Principal 은 JSessionID 를 username 으로 사용하며, UUID 는 이전 원격 연결 과정에서 발급받아 쿠키에 저장해둔 값입니다.
     * Controller 에서는 쿠키에서 UUID 를 가져와서 이 메서드를 호출합니다.
     * @param principal
     * @param userUUID
     * @since pre-1.0.0
     */
    public void cacheUserPrincipalAndUuidForAutoRemote(Principal principal, String userUUID) {
        autoRemoteService.validateAndCacheUserToRedis(principal, userUUID);
    }

    public RemoteCode connectToPrevFormedAutoRemoteGroup(Principal principal, String nickname) {
        return autoRemoteService.connectToPrevFormedAutoRemoteGroup(principal, nickname);
    }

    /**
     * 신규 연결을 생성(newlyForm) 합니다.
     *
     * @param remoteCode
     * @return
     */
    public UUID joinNewlyFormedAutoGroup(RemoteCode remoteCode, Principal principal) {
        return autoRemoteService.joinNewlyFormedAutoGroup(remoteCode, principal);
    }

    public UUID getUuidForCookieFromPrincipalName(String principalName) {
        return autoRemoteService.findPreCachedUserUUID(principalName);
    }

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
            throw new IllegalArgumentException("noshow:코드 연결 에러. 유효하지 않은 코드입니다.");
        }
        if (Strings.isEmpty(nickname)) {
            throw new IllegalArgumentException("noshow:코드 연결 에러. 닉네임이 비어있습니다.");
        }
        remoteCodeService.addSubscriber(remoteCode, principal.getName(), nickname);
    }

    public List<String> getRemoteMembers(RemoteCode remoteCode) {
        List<String> remoteMembers = remoteCodeService.getSubscribers(remoteCode.getRemoteCode())
                .entrySet().stream()
                .map(entry -> (String) entry.getValue())
                .collect(Collectors.toList());
        log.info("remoteMembers : {}", remoteMembers);
        return remoteMembers;
    }

    /**
     * exit user from remote channel by remoteCode and user principal.
     * @param remoteCode
     * @param principal
     */
    public void exitUser(RemoteCode remoteCode, Principal principal) {
        boolean isEmpty = remoteCodeService.removeSubscriber(remoteCode, principal.getName());
        if(isEmpty) {
            autoRemoteService.deactivateAutoGroupIfExist(remoteCode);
        }
    }

    public List<List<String>> getRemoteUserDetails(RemoteCode remoteCode) {
        Map<Object, Object> subscribers = remoteCodeService.getSubscribers(remoteCode.getRemoteCode());
        List<String> keys = subscribers.keySet().stream()
                .map(Object::toString)
                .toList();
        List<String> values = subscribers.values().stream()
                .map(Object::toString)
                .toList();
        return List.of(keys, values);
    }

    public void cacheUserBeforeAutoRemote(Principal principal, String userUUID) {
        this.cacheUserPrincipalAndUuidForAutoRemote(principal, userUUID);
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

    public RemoteConnectResponse autoRemoteReconnect(Principal principal, String nickname) {
        RemoteCode reconnectRemoteCode = this.connectToPrevFormedAutoRemoteGroup(principal, nickname);
        log.info("Auto Remote Reconnected : code = {}", reconnectRemoteCode);
        return new RemoteConnectResponse(reconnectRemoteCode.getRemoteCode(), true, "autoreconnect");
    }

    public boolean isValidCode(RemoteCode remoteCode) {
        return remoteCodeService.isValidCode(remoteCode);
    }
}
