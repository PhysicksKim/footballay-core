package com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.service;

import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.entity.AnonymousUser;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.entity.AutoRemoteGroup;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.repository.AnonymousUserRepository;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.repository.AutoRemoteGroupRepository;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.repository.AutoRemoteRedisRepository;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.service.RedisRemoteCodeService;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.RemoteCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class AutoRemoteService {

    private final RedisRemoteCodeService remoteCodeService;
    private final AutoRemoteGroupRepository groupRepository;
    private final AnonymousUserRepository userRepository;
    private final AutoRemoteRedisRepository autoRemoteRedisRepository;

    /**
     * <pre>
     * 자동 원격 그룹에 연결합니다.
     * 1) 활성화 되지 않은 경우 - 아직 그룹에 원격 코드가 발급되지 않은 경우
     * 해당 자동 원격 그룹에 RemoteCode 를 발급해주고, Redis 의 ActiveAutoRemoteGroups Hash 에 해당 자동 원격 그룹을 추가합니다.
     * 2) 활성화 되어 있는 경우 - 발급된 원격 코드가 있는 경우
     * RemoteCode 에 해당 사용자를 subscriber 로 추가합니다.
     * <br>
     * {key=ActiveAutoRemoteGroups,hash{key=remoteGroupId,value=remoteCode}}
     * {key=RemoteCode:{remoteCode}, hash{key=subscribers, value=nickname}}
     * </pre>
     * @param principal
     * @param nickname
     * @return
     */
    public RemoteCode connect(Principal principal, String nickname) {
        if (principal == null || !StringUtils.hasText(nickname)) {
            throw new IllegalArgumentException("잘못된 요청입니다. 사용자 UUID 또는 Principal 이 존재하지 않습니다.");
        }

        String userUuid = findPreCachedUserUUID(principal);
        if (userUuid == null) {
            throw new IllegalArgumentException("존재하지 않는 익명 유저 UUID 입니다.");
        }
        UUID uuid = UUID.fromString(userUuid);
        AnonymousUser findUser = userRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 익명 유저가 없습니다."));

        AutoRemoteGroup autoRemoteGroup = findUser.getAutoRemoteGroup();
        String findRemoteCode = getRemoteCodeOf(autoRemoteGroup);
        RemoteCode remoteCode;

        if (isRemoteCodeExist(findRemoteCode)) {
            log.info("RemoteCode 가 존재합니다. RemoteCode: {}", findRemoteCode);
            remoteCode = RemoteCode.of(findRemoteCode);
            remoteCodeService.addSubscriber(remoteCode, principal.getName(), nickname);
        } else {
            log.info("RemoteCode 가 존재하지 않습니다. RemoteCode 를 발급합니다.");
            remoteCode = remoteCodeService.generateCodeAndSubscribe(principal.getName(), nickname);
            activateAutoRemoteGroup(autoRemoteGroup, remoteCode);
        }
        return remoteCode;
    }

    protected void activateAutoRemoteGroup(AutoRemoteGroup autoRemoteGroup, RemoteCode remoteCode) {
        log.info("activated Key Pair :: AutoRemoteGroup = {} , RemoteCode = {}", autoRemoteGroup, remoteCode);
        autoRemoteRedisRepository.setActiveAutoRemoteKeyPair(autoRemoteGroup.getId().toString(), remoteCode.getRemoteCode());
    }

    private String getRemoteCodeOf(AutoRemoteGroup autoRemoteGroup) {
        return autoRemoteRedisRepository
                .findRemoteCodeFromAutoGroupId(autoRemoteGroup.getId().toString());
    }

    private String findPreCachedUserUUID(Principal principal) {
        return autoRemoteRedisRepository
                .findBeforeCacheUUIDFromJsessionid(principal.getName());
    }

    private boolean isRemoteCodeExist(String remoteCode) {
        if (remoteCode == null) {
            return false;
        }
        return StringUtils.hasText(remoteCode);
    }

}