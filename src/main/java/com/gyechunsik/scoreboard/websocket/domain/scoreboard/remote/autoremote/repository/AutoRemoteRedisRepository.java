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

/**
 * <pre>
 * 자동 원격 연결과 관련된 Redis CRUD 를 관리합니다.
 * remote:{remoteCode} 로 구성되는 RemoteCode 연결 채널은
 * RemoteCodeService 와 RemoteCodeRepository 에서 다룹니다.
 * <br>
 * # 관리하는 key
 * 1) 자동 원격 UUID Caching [Value]
 * key : autoremote_usercookie_{principalName}
 * value : {UUID}
 * <br>
 * 2) AutoGroupId to RemoteCode [Value]
 * key : autoremote_groupid_{groupid}
 * value : {remoteCode}
 * <br>
 * 3) RemoteCode to AutoGroupId
 * key : autoremote_remotecode_{remoteCode}
 * value : {groupid}
 * </pre>
 */
@Slf4j
@RequiredArgsConstructor
@Repository
public class AutoRemoteRedisRepository {

    private static final String PREFIX_AUTOREMOTE_COMMON = "autoremote_";
    private static final String IDENTIFIER_BEFORE_CACHE = "usercookie_";
    private static final String IDENTIFIER_GROUP_ID = "groupid_";
    private static final String IDENTIFIER_REMOTE_CODE = "remotecode_";

    private static final Duration EXP_ACTIVE_GROUP = RemoteExpireTimes.ACTIVE_REMOTE_GROUP;
    private static final Duration EXP_USER_PRE_CACHE = RemoteExpireTimes.USER_PRE_CACHING;

    private final StringRedisTemplate stringRedisTemplate;

    public void setActiveAutoRemoteKeyPair(String autoGroupId, String remoteCode) {
        final String KEY_FROM_AUTOGROUP = activeKeyFromGroup(autoGroupId);
        final String KEY_FROM_REMOTECODE = activeKeyFromCode(remoteCode);
        log.info("Set Active Auto Remote Key Pair: {} - {}", autoGroupId, remoteCode);
        // Key log
        log.info("Key from autogroup: {}", KEY_FROM_AUTOGROUP);
        log.info("Key from remotecode: {}", KEY_FROM_REMOTECODE);
        stringRedisTemplate.opsForValue().set(KEY_FROM_AUTOGROUP, remoteCode, EXP_ACTIVE_GROUP);
        stringRedisTemplate.opsForValue().set(KEY_FROM_REMOTECODE, autoGroupId, EXP_ACTIVE_GROUP);
    }

    public void setUserPreCacheForCookie(String principalName, String userId) {
        log.info("setUserPreCacheForCookie Called :: Principal={} , userId={}", principalName, userId);
        final String key = keyForPrincipalToUuid(principalName);
        stringRedisTemplate.opsForValue().set(key, userId, EXP_USER_PRE_CACHE);
    }

    public Optional<String> findUserPreCache(String principalName) {
        final String key = keyForPrincipalToUuid(principalName);
        String value = stringRedisTemplate.opsForValue().get(key);
        return Optional.ofNullable(value);
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
        if (autoGroupId != null && remoteCode != null) {
            setActiveAutoRemoteKeyPair(autoGroupId, remoteCode);
        }
        return Optional.ofNullable(autoGroupId);
    }

    public Optional<String> findPrincipalToUuid(String principalName) {
        String key = keyForPrincipalToUuid(principalName);
        return Optional.ofNullable(stringRedisTemplate.opsForValue().get(key));
    }

    private static String activeKeyFromGroup(String autoGroupId) {
        return PREFIX_AUTOREMOTE_COMMON + IDENTIFIER_GROUP_ID + autoGroupId;
    }

    private static String activeKeyFromCode(String remoteCode) {
        return PREFIX_AUTOREMOTE_COMMON + IDENTIFIER_REMOTE_CODE + remoteCode;
    }

    private static String keyForPrincipalToUuid(String principalName) {
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

    public void removeGroupIfExist(String remoteCode) {
        final String KEY_FROM_REMOTECODE = activeKeyFromCode(remoteCode);
        String autoGroupId = stringRedisTemplate.opsForValue().get(KEY_FROM_REMOTECODE);
        if(autoGroupId == null) return;

        final String KEY_FROM_AUTOGROUP = activeKeyFromGroup(autoGroupId);

        stringRedisTemplate.delete(autoGroupId);
        stringRedisTemplate.delete(remoteCode);
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
