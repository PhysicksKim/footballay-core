package com.gyechunsik.scoreboard.websocket.service;

import com.gyechunsik.scoreboard.config.AbstractRedisTestContainerInit;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.service.RedisRemoteCodeService;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.RemoteCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.security.Principal;
import java.time.Duration;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
public class RedisRemoteCodeServiceTest extends AbstractRedisTestContainerInit {

    @Autowired
    private RedisRemoteCodeService redisRemoteCodeService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private Principal mockPrincipal;

    private static final String REMOTECODE_SET_PREFIX = "remote:";

    @BeforeEach
    void setUp() {
        when(mockPrincipal.getName()).thenReturn("testUser");
    }

    @DisplayName("Remote code 를 생성하고 유효성을 검증 시 통과한다.")
    @Test
    void testGenerateAndValidateCode() throws InterruptedException {
        String nickname = "userNickname";
        RemoteCode remoteCode = redisRemoteCodeService.generateCodeAndSubscribe(mockPrincipal.getName(), nickname);
        log.info("generated remoteCode: {}", remoteCode);
        assertNotNull(remoteCode);
        Thread.sleep(1000); // Wait for the key to expire
        Map<Object, Object> subscribers = redisRemoteCodeService.getSubscribers(remoteCode.getRemoteCode());

        int size = subscribers.size();
        log.info("subscribers size: {}", size);
        subscribers.entrySet().forEach(entry -> {
            log.info("subscriber: {}", entry);
        });

        // then
        assertTrue(redisRemoteCodeService.isValidCode(remoteCode));

        // logging redis data
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode());
        String hostToken = stringRedisTemplate.opsForValue().get(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode() + "-hostToken");
        entries.forEach((k, v) -> {
            log.info("RemoteCode:{} == { key: {}, value: {} }", remoteCode.getRemoteCode(), k, v);
        });
        log.info("hostToken: {}", hostToken);

        // Cleanup
        stringRedisTemplate.delete(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode());
    }

    @DisplayName("Remote code 를 생성하고 subscriber 를 추가하고 삭제합니다.")
    @Test
    void testAddAndRemoveSubscriber() {
        // given
        String hostPrincipalName = "hostUser";
        String hostNickname = "hostKim";
        String memberPrincipalName = "memberUser";
        String memberNickname = "memberLee";
        RemoteCode remoteCode = redisRemoteCodeService.generateCodeAndSubscribe(hostPrincipalName, hostNickname);

        // when
        redisRemoteCodeService.addSubscriber(remoteCode, memberPrincipalName, memberNickname);
        Map<Object, Object> addedSubscribers = redisRemoteCodeService.getSubscribers(remoteCode.getRemoteCode());

        redisRemoteCodeService.removeSubscriber(remoteCode, memberPrincipalName);
        Map<Object, Object> removedSubscribers = redisRemoteCodeService.getSubscribers(remoteCode.getRemoteCode());

        // logging
        log.info("addedSubscribers: {}", addedSubscribers);
        addedSubscribers.forEach((k, v) -> {
            log.info("addedSubscriber: { key: {}, value: {} }", k, v);
        });
        log.info("removedSubscribers: {}", removedSubscribers);
        removedSubscribers.forEach((k, v) -> {
            log.info("removedSubscriber: { key: {}, value: {} }", k, v);
        });

        // then
        assertThat(addedSubscribers)
                .containsEntry(hostPrincipalName, hostNickname)
                .containsEntry(memberPrincipalName, memberNickname)
                .size().isEqualTo(2);
        assertThat(removedSubscribers)
                .containsEntry(hostPrincipalName, hostNickname)
                .size().isEqualTo(1);

        // Cleanup
        stringRedisTemplate.delete(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode());
    }

    @DisplayName("Remote code expire 시간을 설정하고 유효성을 검증합니다.")
    @Test
    void testSetExpiration() throws InterruptedException {
        String nickname = "userNickname";

        RemoteCode remoteCode = redisRemoteCodeService.generateCodeAndSubscribe(mockPrincipal.getName(), nickname);
        redisRemoteCodeService.setExpiration(remoteCode, Duration.ofSeconds(1)); // 1 second

        Thread.sleep(1500); // Wait for the key to expire

        assertFalse(redisRemoteCodeService.isValidCode(remoteCode));

        // Cleanup
        stringRedisTemplate.delete(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode());
    }
}
