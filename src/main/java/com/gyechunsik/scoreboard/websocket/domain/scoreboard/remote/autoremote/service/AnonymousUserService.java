package com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.service;

import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.RemoteExpireTimes;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.entity.AutoRemoteGroup;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.repository.AnonymousUserRepository;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.entity.AnonymousUser;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.repository.AutoRemoteRedisRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class AnonymousUserService {

    private final AnonymousUserRepository userRepository;
    private final AutoRemoteRedisRepository autoRemoteRedisRepository;

    private final static Duration CACHE_EXP = RemoteExpireTimes.USER_PRE_CACHING; // 5분

    /**
     * 익명 유저를 생성하고 저장합니다.
     * @param autoRemoteGroup
     * @return
     */
    public AnonymousUser createAndSaveAnonymousUser(AutoRemoteGroup autoRemoteGroup) {
        if(autoRemoteGroup == null) {
            throw new IllegalArgumentException("AutoRemoteGroup 이 존재하지 않습니다.");
        }

        AnonymousUser anonymousUser = new AnonymousUser(); // UUID는 자동으로 생성됩니다.
        anonymousUser.setAutoRemoteGroup(autoRemoteGroup); // AutoRemoteGroup을 설정합니다.
        anonymousUser.setLastConnectedAt(LocalDateTime.now());
        return userRepository.save(anonymousUser);
    }

    public AnonymousUser findUserById(UUID uuid) {
        return userRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 익명 유저입니다."));
    }

    /**
     * 자동 원격 연결을 위해 사용합니다. 원격 연결 이전에 본 메서드를 통해서 Cookie 에 담겨있는 UUID 를 redis 에 캐싱합니다.
     * Cookie 에서 얻은 User UUID 값을 검증하고, 통과하면 Redis 에 Value {key=Principal.Name, value=UUID} 로 캐싱합니다.
     * Cookie 에서 얻은 User UUID 가 DB 에 존재하지 않으면, IllegalArgumentException 을 던집니다.
     * @param principal
     * @param userUUID
     */
    public void validateAndCacheUserToRedis(Principal principal, String userUUID) {
        if(principal == null || !StringUtils.hasText(userUUID)) {
            throw new IllegalArgumentException("잘못된 요청입니다. 사용자 UUID 또는 Principal 이 존재하지 않습니다.");
        }

        UUID uuid = UUID.fromString(userUUID);
        AnonymousUser findUser = userRepository.findById(uuid)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 익명 유저입니다."));

        autoRemoteRedisRepository
                .setUserPreCache(principal.getName(), userUUID);

        // TODO : test 로 LastConnected 제대로 업데이트 되는지 체크 필요
        findUser.setLastConnectedAt(LocalDateTime.now());
    }
}
