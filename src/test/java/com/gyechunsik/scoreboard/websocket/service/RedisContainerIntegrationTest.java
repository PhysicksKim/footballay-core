package com.gyechunsik.scoreboard.websocket.service;

import com.gyechunsik.scoreboard.config.TestContainerConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

@Slf4j
@SpringBootTest
@ExtendWith(TestContainerConfig.class)
@Testcontainers
public class RedisContainerIntegrationTest {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void exampleTest() {
        Long add = stringRedisTemplate.opsForSet().add("testSet", "testValue");
        log.info("Add: {}", add);
    }
}
