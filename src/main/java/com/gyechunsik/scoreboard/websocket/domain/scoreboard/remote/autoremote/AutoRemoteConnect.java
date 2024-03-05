package com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote;

import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.entity.AutoRemoteGroup;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.service.AnonymousUserService;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.service.AutoRemoteGroupService;
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
public class AutoRemoteConnect {

    private final AnonymousUserService anonymousUserService;
    private final AutoRemoteGroupService autoRemoteGroupService;
    private final AutoRemoteService autoRemoteService;

    /**
     * 자동 연결 과정에서 사용할 Principal - UUID 쌍을 Redis 에 캐싱합니다.
     * Principal 은 JSessionID 를 username 으로 사용하며, UUID 는 이전 원격 연결 과정에서 발급받아 쿠키에 저장해둔 값입니다.
     * Controller 에서는 쿠키에서 UUID 를 가져와서 이 메서드를 호출합니다.
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
     * <h3>이전에 형성된 그룹에 재연결</h3>
     * <pre>
     * 현재 연결된 Remote Code Group 으로 Auto Remote Group 을 형성합니다.
     * 이미 생성된 Auto Remote Group 이 존재하는 경우 해당 그룹에 추가됩니다.
     * </pre>
     * <h3>Controller 에서 처리할 작업</h3>
     * <pre>
     * 반환 받은 UUID 를 Cookie 에 저장합니다.
     * UUID Cookie 는 이후 원격 연결에 식별자로 사용됩니다.
     * UUID Cookie 는 3개월을 유효기간으로 설정합니다.
     * </pre>
     * @param uuid
     */
    public boolean rejoinPreviouslyFormedAutoGroup(UUID uuid) {
        AnonymousUser findUser = anonymousUserService.findUserById(uuid);
        AutoRemoteGroup autoRemoteGroup = findUser.getAutoRemoteGroup();

        // TODO : 여기서 부터 다시. 찾은 autoRemoteGroup 으로 redis 에서 active 중인 그룹이 있는지 찾고 분기 나뉨

        return false;
    }

    /**
     * 신규 연결을 생성(newlyForm) 합니다.
     * @param remoteCode
     * @return
     */
    public UUID joinNewlyFormedAutoGroup(RemoteCode remoteCode) {
        Optional<Long> optionalGroupId = autoRemoteGroupService.getActiveGroupIdBy(remoteCode);
        AutoRemoteGroup autoRemoteGroup;
        if(optionalGroupId.isPresent()) {
            autoRemoteGroup = autoRemoteGroupService
                    .getAutoRemoteGroup(optionalGroupId.get())
                    .orElseThrow(() -> new IllegalArgumentException("캐싱된 원격 그룹과 일치하는 원격 그룹이 존재하지 않습니다."));
        } else {
            autoRemoteGroup = autoRemoteGroupService.createAutoRemoteGroup();
            autoRemoteGroupService.activateAutoRemoteGroup(remoteCode, autoRemoteGroup.getId());
        }

        AnonymousUser createdUser = anonymousUserService.createAndSaveAnonymousUser(autoRemoteGroup);
        return createdUser.getId();
    }
}
