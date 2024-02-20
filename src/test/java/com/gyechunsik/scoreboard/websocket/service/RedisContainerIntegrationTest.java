package com.gyechunsik.scoreboard.websocket.service;

import com.gyechunsik.scoreboard.config.AbstractRedisTestContainerInit;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@Slf4j
@SpringBootTest
public class RedisContainerIntegrationTest extends AbstractRedisTestContainerInit {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void exampleTest() {
        Long add = stringRedisTemplate.opsForSet().add("testSet", "testValue");
        log.info("Add: {}", add);
    }
}
