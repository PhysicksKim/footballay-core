package com.gyechunsik.scoreboard.websocket.domain.remote.code;

import com.gyechunsik.scoreboard.domain.token.RemoteHostTokenService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisRemoteCodeService implements RemoteCodeService {

    private final StringRedisTemplate stringRedisTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final RemoteHostTokenService tokenService;

    private static final String REMOTECODE_SET_PREFIX = "remote:";
    private static final String REMOTECODE_HOST_TOKEN_SUFFIX = "-hostToken";
    private static final Duration REMOTECODE_EXPIRATION = Duration.ofSeconds(14400);

    // TODO : Host 용 토큰 발급이 필요한 경우 true 로 변경
    private static final boolean DEV_IS_HOST_TOKEN_ISSUE = false;

    /**
     * 코드를 생성하고 Redis 에 코드 채널을 생성합니다.
     * @param principalName
     * @param nickname
     * @return
     */
    @Override
    public RemoteCode generateCodeAndSubscribe(String principalName, String nickname) {
        RemoteCode remoteCode;
        do {
            remoteCode = RemoteCode.generate();
        } while (stringRedisTemplate.hasKey(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode()));
        log.info("CodeService - generateCodeAndSubscribe: {}", remoteCode.getRemoteCode());

        // 코드 SET 생성 - set 의 key 를 remote:remoteCode 로 하고, value 는 구독자들의 이름으로 한다.
        String REMOTE_CODE_KEY = REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode();
        stringRedisTemplate.opsForHash()
                .put(REMOTE_CODE_KEY, principalName, nickname);

        // Redis 에 token 을 저장
        if(DEV_IS_HOST_TOKEN_ISSUE) {
            String token = tokenService.generateRemoteHostToken(remoteCode.getRemoteCode(), LocalDateTime.now());
            stringRedisTemplate.opsForValue()
                    .set(REMOTE_CODE_KEY + REMOTECODE_HOST_TOKEN_SUFFIX, token);
        }

        // 코드 만료시간 설정
        this.setExpiration(remoteCode, REMOTECODE_EXPIRATION);
        return remoteCode;
    }

    /**
     * RemoteCode 에 구독자 목록 조회
     *
     * @param remoteCode 구독자 목록을 조회할 코드
     * @return 구독자 목록
     */
    @Override
    public Map<Object, Object> getSubscribers(String remoteCode) {
        return stringRedisTemplate.opsForHash().entries(REMOTECODE_SET_PREFIX + remoteCode);
    }

    // TODO : NEED TO IMPLEMENTATION
    @Override
    public Set<String> getNicknames(String remoteCode) {
        return null;
    }

    /**
     * RemoteCode 에 구독자 추가.
     *
     * @param remoteCode 원격제어 코드
     * @param subscriber principal.getName() 으로 식별자를 제공한다.
     * @param nickname   구독자의 닉네임
     * @return
     */
    @Override
    public void addSubscriber(RemoteCode remoteCode, String subscriber, String nickname) {
        stringRedisTemplate.opsForHash().put(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode(), subscriber, nickname);
    }

    /**
     * subscriber 를 명단에서 제외합니다.
     * 삭제 후 더이상 구독자가 없다면 remoteCode 를 삭제합니다.
     *
     * @param remoteCode
     * @param subscriber
     * @return 성공여부
     */
    @Override
    public void removeSubscriber(RemoteCode remoteCode, String subscriber) {
        String remoteCodeKey = REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode();
        stringRedisTemplate.opsForHash().delete(remoteCodeKey, subscriber);

        clearIfEmptyRemoteCode(remoteCode.getRemoteCode());
    }

    /**
     * RemoteCode 만료 시간 설정
     *
     * @param remoteCode
     * @param duration
     */
    @Override
    public void setExpiration(RemoteCode remoteCode, Duration duration) {
        String remoteCodeKey = REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode();
        stringRedisTemplate.expire(remoteCodeKey, duration);
    }

    @Override
    public void refreshExpiration(RemoteCode remoteCode) {
        this.setExpiration(remoteCode, REMOTECODE_EXPIRATION);
    }

    @Override
    public boolean isValidCode(@NotNull RemoteCode remoteCode) {
        log.info("hasKey : {}", stringRedisTemplate.hasKey(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode()));
        return stringRedisTemplate.hasKey(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode());
    }

    /**
     * 코드를 만료시킵니다. 만료된 코드는 삭제됩니다.
     *
     * @param remoteCode
     * @return
     */
    @Override
    public boolean expireCode(RemoteCode remoteCode) {
        // 삭제하는 코드의 구독자들에게 코드가 만료됨을 알려준다.
        final String remoteCodeKey = REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode();
        Set<Object> subs = getSubscribers(remoteCode.getRemoteCode()).keySet();
        if (!stringRedisTemplate.delete(remoteCode.getRemoteCode())) {
            // 코드 삭제 실패
            return false;
        }

        subs.forEach(sub ->
                messagingTemplate.convertAndSendToUser((String) sub, "/topic/remote/" + remoteCode.getRemoteCode(), remoteCode.getRemoteCode())
        );
        return true;
    }

    private boolean clearIfEmptyRemoteCode(String remoteCode) {
        Long size = stringRedisTemplate.opsForHash().size(REMOTECODE_SET_PREFIX + remoteCode);
        if (size == 0) {
            return stringRedisTemplate.delete(REMOTECODE_SET_PREFIX + remoteCode);
        }
        return false;
    }

    protected void removeAllRemoteCodes() {
        log.info("!!! TEST UTIL METHOD :: removed All Remote Codes in Redis !!!");
        stringRedisTemplate.delete(REMOTECODE_SET_PREFIX + "*");
    }

    protected Set<String> getAllRemoteCodes() {
        log.info("!!! TEST UTIL METHOD :: get All Remote Codes in Redis !!!");
        Set<String> keys = stringRedisTemplate.keys(REMOTECODE_SET_PREFIX + "*");
        return keys;
    }

}
