package com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.repository;

import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.RemoteExpireTimes;
import io.jsonwebtoken.lang.Strings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
@Repository
public class AutoRemoteRedisRepository {

    /**
     * AutoRemoteConnect 와 관련된 Redis CRUD 를 관리합니다.
     * remote:{remoteCode} 로 구성되는 RemoteCode 연결 채널은
     * RemoteCodeService 와 RemoteCodeRepository 에서 다룹니다.
     * <p>
     * # 관리하는 key
     * 1) 자동 원격 Before Caching [Value]
     * key : autoremote_beforecache_{principalName}
     * value : UUID
     * <p>
     * 2) AutoGroupId to RemoteCode [Value]
     * key : autoremote_groupid_{groupid}
     * value : {remoteCode}
     * <p>
     * 3) RemoteCode to AutoGroupId
     * key : autoremote_remotecode_{remoteCode}
     * value : {groupid}
     */
    private static final String PREFIX_AUTOREMOTE_COMMON = "autoremote_";
    private static final String IDENTIFIER_BEFORE_CACHE = "beforecache_";
    private static final String IDENTIFIER_GROUP_ID = "groupid_";
    private static final String IDENTIFIER_REMOTE_CODE = "remotecode_";

    private static final Duration EXP_ACTIVE_GROUP = RemoteExpireTimes.ACTIVE_REMOTE_GROUP;
    private static final Duration EXP_USER_PRE_CACHE = RemoteExpireTimes.USER_PRE_CACHING;

    private final StringRedisTemplate stringRedisTemplate;

    public void setActiveAutoRemoteKeyPair(String autoGroupId, String remoteCode) {
        final String KEY_FROM_AUTOGROUP = activeKeyFromGroup(autoGroupId);
        final String KEY_FROM_REMOTECODE = activeKeyFromCode(remoteCode);
        log.info("Set Active AutoRemote Key Pair: {} - {}", autoGroupId, remoteCode);
        // Key log
        log.info("Key: {}", KEY_FROM_AUTOGROUP);
        log.info("Key: {}", KEY_FROM_REMOTECODE);
        stringRedisTemplate.opsForValue().set(KEY_FROM_AUTOGROUP, remoteCode, EXP_ACTIVE_GROUP);
        stringRedisTemplate.opsForValue().set(KEY_FROM_REMOTECODE, autoGroupId, EXP_ACTIVE_GROUP);
    }

    public void setUserPreCache(String principalName, String userId) {
        log.info("setUserPreCache Called :: Principal={} , userId={}", principalName, userId);
        final String key = preCachedUUIDKey(principalName);
        stringRedisTemplate.opsForValue().set(key, userId, EXP_USER_PRE_CACHE);
    }

    public String findRemoteCodeFromAutoGroupId(String autoGroupId) {
        String activeKey = activeKeyFromGroup(autoGroupId);
        log.info("Find RemoteCode from AutoGroupId: {}", autoGroupId);
        log.info("Active Key: {}", activeKey);
        String remoteCode = stringRedisTemplate.opsForValue()
                .get(activeKey);
        if (!Strings.hasText(remoteCode)) {
            return null;
        }
        setActiveAutoRemoteKeyPair(autoGroupId, remoteCode);
        return remoteCode;
    }

    public Optional<String> findAutoGroupIdFromRemoteCode(String remoteCode) {
        String autoGroupId = stringRedisTemplate.opsForValue()
                .get(activeKeyFromCode(remoteCode));
        setActiveAutoRemoteKeyPair(autoGroupId, remoteCode);
        return Optional.ofNullable(autoGroupId);
    }

    public String findBeforeCacheUUIDFromJsessionid(String principalName) {
        String key = preCachedUUIDKey(principalName);
        return stringRedisTemplate.opsForValue().get(key);
    }

    private static String activeKeyFromGroup(String autoGroupId) {
        return PREFIX_AUTOREMOTE_COMMON + IDENTIFIER_GROUP_ID + autoGroupId;
    }

    private static String activeKeyFromCode(String remoteCode) {
        return PREFIX_AUTOREMOTE_COMMON + IDENTIFIER_REMOTE_CODE + remoteCode;
    }

    private static String preCachedUUIDKey(String principalName) {
        return PREFIX_AUTOREMOTE_COMMON + IDENTIFIER_BEFORE_CACHE + principalName;
    }

    public void removeAllActiveGroups() {
        Set<String> activeFromGroupKeys = stringRedisTemplate
                .keys(PREFIX_AUTOREMOTE_COMMON + IDENTIFIER_GROUP_ID + '*');
        Set<String> activeFromRemoteKeys = stringRedisTemplate
                .keys(PREFIX_AUTOREMOTE_COMMON + IDENTIFIER_REMOTE_CODE + '*');

        if (activeFromGroupKeys != null) {
            activeFromGroupKeys.forEach(stringRedisTemplate::delete);
        }
        if (activeFromRemoteKeys != null) {
            activeFromRemoteKeys.forEach(stringRedisTemplate::delete);
        }
    }

    public Map<String, String> getAllActiveGroups() {
        Set<String> activeFromGroupKeys = stringRedisTemplate
                .keys(PREFIX_AUTOREMOTE_COMMON + IDENTIFIER_GROUP_ID + '*');
        Set<String> activeFromRemoteKeys = stringRedisTemplate
                .keys(PREFIX_AUTOREMOTE_COMMON + IDENTIFIER_REMOTE_CODE + '*');

        Map<String, String> map = new HashMap<>();
        for (String key : activeFromRemoteKeys) {
            String value = stringRedisTemplate.opsForValue().get(key);
            map.put(key, value);
        }
        for (String key : activeFromGroupKeys) {
            String value = stringRedisTemplate.opsForValue().get(key);
            map.put(key, value);
        }
        return map;
    }

}
