package com.gyechunsik.scoreboard.websocket.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.time.Duration;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemoryRedisRemoteCodeService implements RemoteCodeService {

    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    private static final String REMOTECODE_SET_PREFIX = "remote:";
    private static final Duration REMOTECODE_EXPIRATION = Duration.ofSeconds(60);

    @Override
    public RemoteCode generateCode(Principal principal) {
        RemoteCode remoteCode;
        do {
            remoteCode = RemoteCode.generate();
        } while (stringRedisTemplate.hasKey(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode()));
        log.info("CodeService - generateCode: {}", remoteCode.getRemoteCode());


        // 코드 SET 생성 - set 의 key 를 remote:remoteCode 로 하고, value 는 구독자들의 이름으로 한다.
        String REMOTE_CODE_KEY = REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode();
        stringRedisTemplate.opsForSet().add(REMOTE_CODE_KEY, principal.getName());

        // 코드 만료시간 설정
        this.setExpiration(remoteCode, REMOTECODE_EXPIRATION);
        return remoteCode;
    }

    @Override
    public boolean isValidCode(RemoteCode remoteCode) {
        return stringRedisTemplate.hasKey(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode());
    }

    /**
     * 코드를 만료시킵니다. 만료된 코드는 삭제됩니다.
     * @param remoteCode
     * @return
     */
    @Override
    public boolean expireCode(RemoteCode remoteCode) {
        // 삭제하는 코드의 구독자들에게 코드가 만료됨을 알려준다.
        Set<String> subs = getSubscribers(remoteCode);
        if (!stringRedisTemplate.delete(remoteCode.getRemoteCode())) {
            // 코드 삭제 실패
            return false;
        }

        subs.forEach(sub -> messagingTemplate.convertAndSendToUser(sub, "/topic/remote/"+remoteCode.getRemoteCode(), remoteCode.getRemoteCode()));
        return true;
    }

    /**
     * RemoteCode 에 구독자 목록 조회
     * @param remoteCode 구독자 목록을 조회할 코드
     * @return 구독자 목록
     */
    @Override
    public Set<String> getSubscribers(RemoteCode remoteCode) {
        return stringRedisTemplate.opsForSet().members(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode());
    }

    /**
     * RemoteCode 에 구독자 추가.
     * @param remoteCode 원격제어 코드
     * @param subscriber principal.getName() 으로 식별자를 제공한다.
     * @return
     */
    @Override
    public boolean addSubscriber(RemoteCode remoteCode, String subscriber) {
        return stringRedisTemplate.opsForSet().add(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode(), subscriber) > 0;
    }

    /**
     * subscriber 를 명단에서 제외합니다.
     * @param remoteCode
     * @param subscriber
     * @return 성공여부
     */
    @Override
    public boolean removeSubscriber(RemoteCode remoteCode, String subscriber) {
        return stringRedisTemplate.opsForSet().remove(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode(), subscriber) > 0;
    }

    /**
     * RemoteCode 만료 시간 설정
     * @param remoteCode
     * @param duration
     */
    @Override
    public void setExpiration(RemoteCode remoteCode, Duration duration) {
        String remoteCodeKey = REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode();
        stringRedisTemplate.expire(remoteCodeKey, duration);
    }


    public void setExpiration(RemoteCode remoteCode) {
        this.setExpiration(remoteCode, REMOTECODE_EXPIRATION);
    }
}
