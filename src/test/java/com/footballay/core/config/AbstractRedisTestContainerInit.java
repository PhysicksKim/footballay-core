package com.footballay.core.config;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Testcontainer 라이브러리에서는 Redis Container 를 제공할 때 "Random Port" 로 실행시킵니다.
 * 따라서 Redis Container 가 실행된 Port 가 Random 이므로, 이를 Spring 에 알려줘야지 Redis 관련 Bean 들을 정상적으로 생성하고 테스트할 수 있습니다.
 * 예를 들어 StringRedisTemplate 같은 객체는 Redis connection 을 얻어야 하는데, Spring 에서는 property 값이 설정되지 않는다면 default 로 6379 port 로 Redis 에 연결하려고 시도합니다.
 * 그러나 Testcontainer 는 Random Port 로 Redis Container 를 실행시키므로, 이를 property 값을 설정해서 Spring 에게 알려주어야 합니다.
 * 따라서 Testcontainer 를 사용해서 Redis Container 를 실행시킨 후 그 Port 를 Spring 에게 알려주기 위해 property 를 설정해줘야 합니다.
 * 이를 위해서 DynamicPropertySource 를 사용해 redis 의 host 와 port 를 알려주도록 합니다.
 * 추가로 redis 최신버전에서는 필수적으로 password 를 설정해야 하기 때문에 password 도 지정해주도록 합니다.
 *
 * @usage RedisTestClass extend AbstractRedisTestContainerInit
 */
abstract public class AbstractRedisTestContainerInit {

    protected static final GenericContainer redisContainer;

    static {
        // Redis 컨테이너 초기화
        redisContainer = new GenericContainer(DockerImageName.parse("redis:7.0.15-alpine"))
                .withExposedPorts(6379);
        redisContainer.start();
    }

    // 스프링 테스트 환경에 동적으로 Redis 컨테이너의 포트 정보를 주입
    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", redisContainer::getHost);
        registry.add("spring.data.redis.port", () -> redisContainer.getMappedPort(6379));
        registry.add("spring.data.redis.password", () -> "1234");
    }

}