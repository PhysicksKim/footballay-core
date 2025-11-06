package com.footballay.core.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.utility.DockerImageName;

public class TestRedisInitializer
        implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final RedisContainer REDIS =
            new RedisContainer(DockerImageName.parse("redis:7.4.5-alpine"))
                    .withReuse(true);

    static {
        REDIS.start();
    }

    @Override
    public void initialize(ConfigurableApplicationContext context) {
        TestPropertyValues.of(
                "spring.data.redis.host=" + REDIS.getHost(),
                "spring.data.redis.port=" + REDIS.getFirstMappedPort(),
                "spring.data.redis.password=1234"
        ).applyTo(context.getEnvironment());
    }
}
