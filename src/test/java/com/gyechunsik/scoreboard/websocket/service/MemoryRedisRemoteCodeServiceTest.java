package com.gyechunsik.scoreboard.websocket.service;

import com.gyechunsik.scoreboard.config.AbstractRedisTestContainerInit;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
public class MemoryRedisRemoteCodeServiceTest extends AbstractRedisTestContainerInit {

    @Autowired
    private MemoryRedisRemoteCodeService memoryRedisRemoteCodeService ;

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
        RemoteCode remoteCode = memoryRedisRemoteCodeService.generateCode(mockPrincipal, nickname);
        log.info("generated remoteCode: {}", remoteCode);
        assertNotNull(remoteCode);
        Thread.sleep(1000); // Wait for the key to expire
        Map<Object, Object> subscribers = memoryRedisRemoteCodeService.getSubscribers(remoteCode.getRemoteCode());

        int size = subscribers.size();
        log.info("subscribers size: {}", size);
        subscribers.entrySet().forEach(entry -> {
            log.info("subscriber: {}", entry);
        });

        assertTrue(memoryRedisRemoteCodeService.isValidCode(remoteCode));

        // Cleanup
        stringRedisTemplate.delete(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode());
    }

    @DisplayName("Remote code 를 생성하고 subscriber 를 추가하고 삭제합니다.")
    @Test
    void testAddAndRemoveSubscriber() {
        String subscriber = "anotherUser";
        String nickname = "userNicknameKim";

        RemoteCode remoteCode = memoryRedisRemoteCodeService.generateCode(mockPrincipal, nickname);

        memoryRedisRemoteCodeService.addSubscriber(remoteCode, subscriber, nickname);
        Map<Object, Object> addedSubscribers = memoryRedisRemoteCodeService.getSubscribers(remoteCode.getRemoteCode());

        memoryRedisRemoteCodeService.removeSubscriber(remoteCode, subscriber);
        Map<Object, Object> removedSubscribers = memoryRedisRemoteCodeService.getSubscribers(remoteCode.getRemoteCode());

        Assertions.assertThat(addedSubscribers).containsEntry(subscriber, nickname).size().isEqualTo(1);
        Assertions.assertThat(removedSubscribers).isEmpty();

        // Cleanup
        stringRedisTemplate.delete(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode());
    }

    @DisplayName("Remote code expire 시간을 설정하고 유효성을 검증합니다.")
    @Test
    void testSetExpiration() throws InterruptedException {
        String nickname = "userNickname";

        RemoteCode remoteCode = memoryRedisRemoteCodeService.generateCode(mockPrincipal, nickname);
        memoryRedisRemoteCodeService.setExpiration(remoteCode, Duration.ofSeconds(1)); // 1 second

        Thread.sleep(1500); // Wait for the key to expire

        assertFalse(memoryRedisRemoteCodeService.isValidCode(remoteCode));

        // Cleanup
        stringRedisTemplate.delete(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode());
    }
}
