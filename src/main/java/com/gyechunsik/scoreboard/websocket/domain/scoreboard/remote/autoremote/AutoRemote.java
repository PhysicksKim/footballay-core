package com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote;

import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.entity.AutoRemoteGroup;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.entity.AnonymousUser;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.service.AutoRemoteService;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.RemoteCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class AutoRemote {

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
        if (principal == null || !StringUtils.hasText(userUUID)) {
            log.info("Principal: {}", principal);
            log.info("userUUID: {}", userUUID);
            throw new IllegalArgumentException("noshow:잘못된 요청입니다. 사용자 UUID 또는 Principal 이 존재하지 않습니다.");
        }
        autoRemoteService.validateAndCacheUserToRedis(principal, userUUID);
    }

    public RemoteCode connectToPrevFormedAutoRemoteGroup(Principal principal, String nickname) {
        if (principal == null || !StringUtils.hasText(nickname)) {
            throw new IllegalArgumentException("noshow:잘못된 요청입니다. 사용자 UUID 또는 Principal 이 존재하지 않습니다.");
        }

        RemoteCode remoteCode = autoRemoteService.connectToPrevFormedAutoRemoteGroup(principal, nickname);
        log.info("RemoteCode: {}", remoteCode);
        return remoteCode;
    }

    /**
     * 신규 연결을 생성(newlyForm) 합니다.
     * @param remoteCode
     * @return
     */
    public UUID joinNewlyFormedAutoGroup(RemoteCode remoteCode, Principal principal) {
        Optional<Long> optionalGroupId = autoRemoteService.getActiveGroupIdBy(remoteCode);
        AutoRemoteGroup autoRemoteGroup;
        if(optionalGroupId.isPresent()) {
            // 이미 누군가 자동 연결 그룹을 생성했다면, 기존 그룹에 참여합니다.
            log.info("기존에 생성된 그룹에 참여");
            autoRemoteGroup = autoRemoteService
                    .getAutoRemoteGroup(optionalGroupId.get())
                    .orElseThrow(() -> new IllegalArgumentException("noshow:캐싱된 원격 그룹과 일치하는 원격 그룹이 존재하지 않습니다."));
        } else {
            // 아직 자동 연결 그룹이 생성되지 않았다면, 새로 그룹을 만듭니다.
            log.info("기존 그룹이 없으므로 새롭게 그룹 생성");
            autoRemoteGroup = autoRemoteService.createAutoRemoteGroup();
            log.info("autoRemoteGroup : {}", autoRemoteGroup);
            log.info("autoRemoteGroup id : {}", autoRemoteGroup.getId());
            autoRemoteService.activateAutoRemoteGroup(remoteCode, autoRemoteGroup.getId());
        }

        AnonymousUser createdUser = autoRemoteService.createAndSaveAnonymousUser(autoRemoteGroup);
        UUID uuid = createdUser.getId();

        cacheUserPrincipalAndUuidForAutoRemote(principal, uuid.toString());
        return createdUser.getId();
    }

    public UUID getUuidForCookieFromPrincipalName(String principalName) {
        String preCachedUserUUID = autoRemoteService.findPreCachedUserUUID(principalName);
        log.info("get uuid for cookie from principalName : {}", preCachedUserUUID);
        return UUID.fromString(preCachedUserUUID);
    }
}
