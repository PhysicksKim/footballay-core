package com.footballay.core.monitor.alert.duplicate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RedisAlertDeduplicatorTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    private RedisAlertDeduplicator deduplicator;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        deduplicator = new RedisAlertDeduplicator(redisTemplate);
    }

    @Test
    void shouldNotify_returnsTrue_whenSetIfAbsentTrue() {
        String type = "T";
        String id = "1";
        Duration ttl = Duration.ofSeconds(60);
        String key = "alert:T:1";

        when(valueOps.setIfAbsent(key, "1", ttl)).thenReturn(Boolean.TRUE);

        assertTrue(deduplicator.shouldNotify(type, id, ttl));

        verify(valueOps).setIfAbsent(key, "1", ttl);
    }

    @Test
    void shouldNotify_returnsFalse_whenSetIfAbsentFalse() {
        String type = "T";
        String id = "2";
        Duration ttl = Duration.ofSeconds(30);
        String key = "alert:T:2";

        when(valueOps.setIfAbsent(key, "1", ttl)).thenReturn(Boolean.FALSE);

        assertFalse(deduplicator.shouldNotify(type, id, ttl));

        verify(valueOps).setIfAbsent(key, "1", ttl);
    }

    @Test
    void invalidate_deletesKey() {
        String type = "X";
        String id = "42";
        String key = "alert:X:42";

        deduplicator.invalidate(type, id);

        verify(redisTemplate).delete(key);
    }
}
