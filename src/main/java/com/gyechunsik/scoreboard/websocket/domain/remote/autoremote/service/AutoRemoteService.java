package com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.service;

import com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.entity.AnonymousUser;
import com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.entity.AutoRemoteGroup;
import com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.repository.AnonymousUserRepository;
import com.gyechunsik.scoreboard.websocket.domain.remote.code.RedisRemoteCodeService;
import com.gyechunsik.scoreboard.websocket.domain.remote.code.RemoteCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class AutoRemoteService {

    private final RedisRemoteCodeService remoteCodeService;
    private final AnonymousUserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;

    private final static String ACTIVE_AUTO_REMOTE_GROUPS = "ActiveAutoRemoteGroups";

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
     *
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
        Object findRemoteCode = getRemoteCodeOf(autoRemoteGroup);
        RemoteCode remoteCode;

        if (isRemoteCodeExist(findRemoteCode)) {
            remoteCode = RemoteCode.of((String) findRemoteCode);
            remoteCodeService.addSubscriber(remoteCode, principal.getName(), nickname);
        } else {
            remoteCode = remoteCodeService.generateCodeAndSubscribe(principal.getName(), nickname);
            activateAutoRemoteGroup(autoRemoteGroup, remoteCode);
        }
        return remoteCode;
    }

    protected void activateAutoRemoteGroup(AutoRemoteGroup autoRemoteGroup, RemoteCode remoteCode) {
        stringRedisTemplate.opsForHash()
                .put(ACTIVE_AUTO_REMOTE_GROUPS, autoRemoteGroup.getId().toString(), remoteCode.getRemoteCode());
    }

    private Object getRemoteCodeOf(AutoRemoteGroup autoRemoteGroup) {
        return stringRedisTemplate.opsForHash()
                .get(ACTIVE_AUTO_REMOTE_GROUPS, autoRemoteGroup.getId().toString());
    }

    private String findPreCachedUserUUID(Principal principal) {
        return stringRedisTemplate.opsForValue()
                .get(principal.getName());
    }

    private boolean isRemoteCodeExist(Object remoteCodeObj) {
        if (remoteCodeObj == null) {
            return false;
        }
        String remoteCode = (String) remoteCodeObj;
        return StringUtils.hasText(remoteCode);
    }

    protected void removeAllActivatedAutoRemoteGroups() {
        stringRedisTemplate.delete(ACTIVE_AUTO_REMOTE_GROUPS);
    }

    protected Map<String, String> getActiveAutoRemoteGroups() {
        Map<Object, Object> entries = stringRedisTemplate.opsForHash()
                .entries(ACTIVE_AUTO_REMOTE_GROUPS);
        return entries.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        entry -> (String) entry.getKey(),
                        entry -> (String) entry.getValue()
                ));
    }
}