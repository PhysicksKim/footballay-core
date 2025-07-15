package com.footballay.core.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;

@Slf4j
@ActiveProfiles("mockapi")
@SpringBootTest
public class RedisContainerIntegrationTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void exampleTest() {
        Long add = stringRedisTemplate.opsForSet().add("testSet", "testValue");
        log.info("Add: {}", add);
    }
}
