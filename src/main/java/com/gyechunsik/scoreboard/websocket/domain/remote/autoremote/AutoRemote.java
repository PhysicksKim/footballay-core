package com.gyechunsik.scoreboard.websocket.domain.remote.autoremote;

import com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.entity.AutoRemoteGroup;
import com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.service.AnonymousUserService;
import com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.service.AutoRemoteGroupService;
import com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.entity.AnonymousUser;
import com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.service.AutoRemoteService;
import com.gyechunsik.scoreboard.websocket.domain.remote.code.RemoteCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class AutoRemote {

    private final AnonymousUserService anonymousUserService;
    private final AutoRemoteGroupService autoRemoteGroupService;
    private final AutoRemoteService autoRemoteService;

    /**
     * 자동 연결 과정에서 사용할 Principal - UUID 쌍을 Redis 에 캐싱합니다.
     * Principal 은 JSessionID 를 username 으로 사용하며, UUID 는 이전 원격 연결 과정에서 발급받아 쿠키에 저장해둔 값입니다.
     * Controller 에서는 쿠키에서 UUID 를 가져와서 이 메서드를 호출합니다.
     *
     * @param principal
     * @param userUUID
     * @since pre-1.0.0
     */
    public void cacheUserBeforeAutoRemote(Principal principal, String userUUID) {
        if (principal == null || !StringUtils.hasText(userUUID)) {
            throw new IllegalArgumentException("잘못된 요청입니다. 사용자 UUID 또는 Principal 이 존재하지 않습니다.");
        }
        anonymousUserService.validateAndCacheUserToRedis(principal, userUUID);
    }

    public RemoteCode connect(Principal principal, String nickname) {
        if (principal == null || !StringUtils.hasText(nickname)) {
            throw new IllegalArgumentException("잘못된 요청입니다. 사용자 UUID 또는 Principal 이 존재하지 않습니다.");
        }

        RemoteCode remoteCode = autoRemoteService.connect(principal, nickname);
        log.info("RemoteCode: {}", remoteCode);
        return remoteCode;
    }

    /**
     * <pre>
     * 현재 연결된 Remote Code Group 으로 Auto Remote Group 을 형성합니다.
     * 이미 생성된 Auto Remote Group 이 존재하는 경우 해당 그룹에 추가됩니다.
     *
     * # Controller 에서 처리할 작업
     * 반환 받은 UUID 를 Cookie 에 저장합니다.
     * UUID Cookie 는 이후 원격 연결에 식별자로 사용됩니다.
     * UUID Cookie 는 3개월을 유효기간으로 설정합니다.
     * </pre>
     * @param activeRemoteCode 현재 연결된 Remote Code
     * @return UUID 사용자 식별자로 사용됩니다. Client 는 UUID 를 Cookie 로 저장해야 합니다.
     */
    public UUID enrollUserToAutoRemoteGroup(RemoteCode activeRemoteCode) {
        Optional<String> activeGroupId
                = autoRemoteGroupService.getActiveGroupIdByRemoteCode(activeRemoteCode);
        AutoRemoteGroup autoRemoteGroup;
        if (activeGroupId.isPresent()) {
            // 원격 그룹이 이미 존재하는 경우
            autoRemoteGroup = autoRemoteGroupService
                    .getAutoRemoteGroup(Long.parseLong(activeGroupId.get()))
                    .orElseThrow(() -> new IllegalArgumentException("캐싱된 원격 그룹과 일치하는 원격 그룹이 존재하지 않습니다."));
        } else {
            // 원격 그룹이 없는 경우
            autoRemoteGroup = autoRemoteGroupService.createAutoRemoteGroup();
            autoRemoteGroupService.setRemoteCodeToAutoGroupId(activeRemoteCode, autoRemoteGroup.getId());
        }

        AnonymousUser savedAnonymousUser = anonymousUserService.createAndSaveAnonymousUser(autoRemoteGroup);
        UUID userUUID = savedAnonymousUser.getId();
        log.info("AutoRemote Enrolled By User UUID : {}", userUUID);
        return userUUID;
    }

    public void updateGroupExpiration(AutoRemoteGroup group, LocalDateTime newExpirationTime) {
        autoRemoteGroupService.updateExpirationTime(group, newExpirationTime);
    }

    public void reactivateGroup(AutoRemoteGroup group) {
        autoRemoteGroupService.reactivateGroup(group);
    }

}
