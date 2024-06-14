package com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.service;

import com.gyechunsik.scoreboard.config.AbstractRedisTestContainerInit;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.service.AutoRemoteService;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.RemoteCode;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.service.RemoteCodeService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@ActiveProfiles("mockapi")
@SpringBootTest
class AutoRemoteGroupServiceTest extends AbstractRedisTestContainerInit {

    @Autowired
    private AutoRemoteService autoRemoteService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private RemoteCodeService remoteCodeService;

    @Mock
    private Principal principal;

    private static final String MOCK_PRINCIPAL_NAME = "mockPrincipalName";

    @BeforeEach
    void setUp() {
        Mockito.when(principal.getName()).thenReturn(MOCK_PRINCIPAL_NAME);
    }
    @AfterEach
    void cleanUp() {
        stringRedisTemplate.delete(stringRedisTemplate.keys("*"));
    }

    @Transactional
    @DisplayName("Active RemoteCode 에 맵핑된 AutoRemoteGroup Id 를 알려주는 Redis Key-Value 생성에 성공합니다")
    @Test
    void Success_RemoteCode_AutoRemoteGroup_Mapping() {
        // given
        RemoteCode remoteCode = remoteCodeService.generateCodeAndSubscribe(principal.getName(), "nickname");
        log.info("RemoteCode: {}", remoteCode);

        // when
        autoRemoteService.activateAutoRemoteGroup(remoteCode, 1L);

        // logging
        Set<String> allKeys = stringRedisTemplate.keys("*");
        log.info("--- All keys ---");
        allKeys.forEach(key -> {
            log.info("Key: {}", key);
        });

        // then
        Set<String> remoteCodeKey = stringRedisTemplate.keys("remote:*");
        assertThat(remoteCodeKey).isNotNull();
        assertThat(remoteCodeKey).hasSize(1);

        List<String> remoteCodeList = remoteCodeKey.stream().toList();
        assertThat(remoteCodeList.get(0)).startsWith("remote:");

        Set<String> autoremoteKeys = stringRedisTemplate.keys("autoremote_*");
        assertThat(autoremoteKeys).isNotNull();
        assertThat(autoremoteKeys).hasSize(2);

        List<String> autoRemoteList = autoremoteKeys.stream().toList();
        assertThat(autoRemoteList).containsAll(Arrays.asList("autoremote_groupid_1", "autoremote_remotecode_"+remoteCode.getRemoteCode()));
    }
}