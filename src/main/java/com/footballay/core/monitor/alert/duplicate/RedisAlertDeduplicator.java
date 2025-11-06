package com.footballay.core.monitor.alert.duplicate;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Redis 기반 알림 중복 제어 구현체
 */
@Service
public class RedisAlertDeduplicator implements AlertDeduplicator {
    private final StringRedisTemplate redis;

    public RedisAlertDeduplicator(StringRedisTemplate redis) {
        this.redis = redis;
    }

    @Override
    public boolean shouldNotify(String type, String id, Duration ttl) {
        String key = getKey(type, id);
        Boolean success = redis.opsForValue()
                .setIfAbsent(key, "1", ttl);
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void invalidate(String type, String id) {
        redis.delete(getKey(type, id));
    }

    private String getKey(String type, String id) {
        return String.format("alert:%s:%s", type, id);
    }
}

