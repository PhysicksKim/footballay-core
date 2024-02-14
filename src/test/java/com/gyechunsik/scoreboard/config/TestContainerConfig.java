package com.gyechunsik.scoreboard.config;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainer 라이브러리에서는 Redis Container 를 제공할 때 "Random Port" 로 실행시킵니다.
 * 따라서 Redis Container 가 실행된 Port 가 Random 이므로, 이를 Spring 에게 알려줘야지 Redis 관련 Bean 들을 정상적으로 생성하고 테스트할 수 있습니다.
 * 예를 들어 StringRedisTemplate 같은 객체는 Redis connection 을 얻어야 하는데, Spring 에서는 고정된 6379 port 로 Redis 에 연결하려고 시도합니다.
 * 그러나 Testcontainer 는 Random Port 로 Redis Container 를 실행시키므로, 이를 Spring 에게 알려주어야 합니다.
 * 따라서 Testcontainer 를 사용해서 Redis Container 를 실행시킨 후 그 Port 를 Spring 에게 알려주는 작업을 수행해야 합니다.
 * 이 클래스(TestContainerConfig)는 Testcontainer 실행 후 host, port 값을 가져와서 동적으로 Spring 에게 Redis Property 값을 설정해서 알려주도록 합니다.
 * @usage @ExtendWith(TestContainerConfig.class)
 * ExtendWith 는 JUnit5 에서 제공하는 확장 모델입니다. 이를 사용하면 테스트 실행 전/후에 추가적인 작업을 수행할 수 있습니다.
 * TetContainerConfig 를 Test 클래스가 실행되기 전에 설정을 위해 사용되도록 합니다.
 */
public class TestContainerConfig implements BeforeAllCallback {

    private static final String REDIS_IMAGE = "redis:7.0.8-alpine";
    private static final int REDIS_PORT = 6379;
    private GenericContainer redis;

    @Override
    public void beforeAll(ExtensionContext context) {
        redis = new GenericContainer(DockerImageName.parse(REDIS_IMAGE))
                .withExposedPorts(REDIS_PORT);
        redis.start();
        System.setProperty("spring.data.redis.host", redis.getHost());
        System.setProperty("spring.data.redis.port", String.valueOf(redis.getMappedPort(REDIS_PORT
        )));
    }
}