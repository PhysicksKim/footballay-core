package com.footballay.core.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
public class RedisContainerIntegrationTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RedisContainerIntegrationTest.class);
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void exampleTest() {
        Long add = stringRedisTemplate.opsForSet().add("testSet", "testValue");
        log.info("Add: {}", add);
    }
}
