package com.gyechunsik.scoreboard.websocket.service;

import com.gyechunsik.scoreboard.config.AbstractRedisTestContainerInit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.security.Principal;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

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
    void testGenerateAndValidateCode() {
        RemoteCode remoteCode = memoryRedisRemoteCodeService.generateCode(mockPrincipal);
        assertNotNull(remoteCode);
        assertTrue(memoryRedisRemoteCodeService.isValidCode(remoteCode));

        // Cleanup
        stringRedisTemplate.delete(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode());
    }

    @DisplayName("Remote code 를 생성하고 subscriber 를 추가하고 삭제합니다.")
    @Test
    void testAddAndRemoveSubscriber() {
        RemoteCode remoteCode = memoryRedisRemoteCodeService.generateCode(mockPrincipal);
        String subscriber = "anotherUser";

        assertTrue(memoryRedisRemoteCodeService.addSubscriber(remoteCode, subscriber));
        assertTrue(memoryRedisRemoteCodeService.getSubscribers(remoteCode.getRemoteCode()).contains(subscriber));
        assertTrue(memoryRedisRemoteCodeService.removeSubscriber(remoteCode, subscriber));
        assertFalse(memoryRedisRemoteCodeService.getSubscribers(remoteCode.getRemoteCode()).contains(subscriber));

        // Cleanup
        stringRedisTemplate.delete(REMOTECODE_SET_PREFIX + remoteCode.getRemoteCode());
    }

    @DisplayName("Remote code expire 시간을 설정하고 유효성을 검증합니다.")
    @Test
    void testSetExpiration() throws InterruptedException {
        RemoteCode remoteCode = memoryRedisRemoteCodeService.generateCode(mockPrincipal);
        memoryRedisRemoteCodeService.setExpiration(remoteCode, Duration.ofSeconds(1)); // 1 second

        Thread.sleep(1500); // Wait for the key to expire

        assertFalse(memoryRedisRemoteCodeService.isValidCode(remoteCode));
    }
}
