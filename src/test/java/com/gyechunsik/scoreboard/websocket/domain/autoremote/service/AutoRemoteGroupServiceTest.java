package com.gyechunsik.scoreboard.websocket.domain.autoremote.service;

import com.gyechunsik.scoreboard.config.AbstractRedisTestContainerInit;
import com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.service.AutoRemoteGroupService;
import com.gyechunsik.scoreboard.websocket.domain.remote.code.RemoteCode;
import com.gyechunsik.scoreboard.websocket.domain.remote.code.RemoteCodeService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@SpringBootTest
class AutoRemoteGroupServiceTest extends AbstractRedisTestContainerInit {

    @Autowired
    private AutoRemoteGroupService autoRemoteGroupService;
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

    /**
     * <pre>
     * # Will Be Cached
     * key : Remote:{remoteCode}_AutoRemoteGroupId
     * value : {AutoRemoteGroupId}
     *
     * # example
     * - key = Remote:2eo39p_AutoRemoteGroupId
     * - value = 1
     * </pre>
     */
    @DisplayName("Active RemoteCode 에 맵핑된 AutoRemoteGroup Id 를 알려주는 Redis Key-Value 생성에 성공합니다")
    @Test
    void Success_RemoteCode_AutoRemoteGroup_Mapping() {
        // given
        RemoteCode remoteCode = remoteCodeService.generateCodeAndSubscribe(principal.getName(), "nickname");
        log.info("RemoteCode: {}", remoteCode);

        // when
        autoRemoteGroupService.setRemoteCodeToAutoGroupId(remoteCode, 1L);

        // then
        Set<String> keys = stringRedisTemplate.keys("Remote:*");
        assertThat(keys).isNotNull();

        keys.forEach(key -> {
            log.info("Key: {}, Value: {}", key, stringRedisTemplate.opsForValue().get(key));
        });
        List<String> collect = keys.stream().collect(Collectors.toList());

        assertThat(keys).isNotEmpty();
        assertThat(collect).hasSize(1);
        assertThat(collect.get(0)).startsWith("Remote:").endsWith("_AutoRemoteGroupId");
    }
}