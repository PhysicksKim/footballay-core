package com.gyechunsik.scoreboard.websocket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Duration;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryRedisRemoteCodeService implements RemoteCodeService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final String REMOTECODE_SET_PREFIX = "remote:";

    @Override
    public RemoteCode generateCode(Principal principal) {
        RemoteCode remoteCode;
        do {
            remoteCode = RemoteCode.generate();
        } while (stringRedisTemplate.hasKey(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode()));
        log.info("CodeService - generateCode: {}", remoteCode.getRemoteCode());

        // 코드 생성자 포함
        stringRedisTemplate.opsForSet().add(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode(), principal.getName());
        return remoteCode;
    }

    @Override
    public boolean isValidCode(RemoteCode remoteCode) {
        return stringRedisTemplate.hasKey(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode());
    }

    @Override
    public boolean expireCode(RemoteCode remoteCode) {
        // TODO : 삭제하는 RemoteCode 구독자들에게 삭제여부를 알려야 함
        return stringRedisTemplate.delete(remoteCode.getRemoteCode());
    }


    public Set<String> getSubscribers(RemoteCode remoteCode) {
        return stringRedisTemplate.opsForSet().members(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode());
    }

    public boolean addSubscriber(RemoteCode remoteCode, String subscriber) {
        return stringRedisTemplate.opsForSet().add(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode(), subscriber) > 0;
    }

    public boolean removeSubscriber(RemoteCode remoteCode, String subscriber) {
        return stringRedisTemplate.opsForSet().remove(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode(), subscriber) > 0;
    }

    // 만료 시간 설정 메소드 추가
    public void setExpiration(RemoteCode remoteCode, long timeoutInSeconds) {
        stringRedisTemplate.expire(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode(), Duration.ofSeconds(timeoutInSeconds));
    }
}
