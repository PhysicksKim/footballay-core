package com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.service;

import com.gyechunsik.scoreboard.config.AbstractRedisTestContainerInit;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.service.RedisRemoteCodeService;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.RemoteCode;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

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

    private static final String REMOTECODE_SET_PREFIX = "remote:";

    @Mock
    private Principal mockFirstPrincipal;

    @Mock
    private Principal mockSecondPrincipal;

    private static final String SUFFIX_MOCK_PRINCIPAL_NAME = "THIS_WILL_BE_JSESSIONID_autoremoteservicetest";
    private static final String FIRST_USER_PRINCIPAL_NAME = "first_" + SUFFIX_MOCK_PRINCIPAL_NAME;
    private static final String SECOND_USER_PRINCIPAL_NAME = "second_" + SUFFIX_MOCK_PRINCIPAL_NAME;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Mockito를 사용하여 목 초기화
        when(mockFirstPrincipal.getName()).thenReturn(FIRST_USER_PRINCIPAL_NAME);
        when(mockSecondPrincipal.getName()).thenReturn(SECOND_USER_PRINCIPAL_NAME);
    }


    @DisplayName("Remote code 를 생성하고 유효성을 검증 시 통과한다.")
    @Test
    void testGenerateAndValidateCode() throws InterruptedException {
        String nickname = "userNickname";
        RemoteCode remoteCode = redisRemoteCodeService.generateCodeAndSubscribe(mockFirstPrincipal.getName(), nickname);
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
    }

    @DisplayName("Remote code expire 시간을 설정하고 유효성을 검증합니다.")
    @Test
    void testSetExpiration() throws InterruptedException {
        String nickname = "userNickname";

        RemoteCode remoteCode = redisRemoteCodeService.generateCodeAndSubscribe(mockFirstPrincipal.getName(), nickname);
        redisRemoteCodeService.setExpiration(remoteCode, Duration.ofSeconds(1)); // 1 second

        Thread.sleep(1500); // Wait for the key to expire

        assertFalse(redisRemoteCodeService.isValidCode(remoteCode));
    }

    @DisplayName("같은 remoteCode 채널에 중복 닉네임이 있는 경우 예외를 반환합니다.")
    @Test
    void fail_duplicate_nickname() {
        // given
        String nickname1 = "user1";

        // when
        RemoteCode remoteCode = redisRemoteCodeService.generateCodeAndSubscribe(mockFirstPrincipal.getName(), nickname1);

        // then
        assertThrows(IllegalArgumentException.class,
                () -> redisRemoteCodeService.addSubscriber(remoteCode, mockSecondPrincipal.getName(), nickname1));
    }

    @DisplayName("채널에 최대 접속 가능 인원 수를 초과하는 경우 예외를 반환합니다.")
    @Test
    void fail_exceed_max_subscriber() {
        // given
        String hostNickname = "hostUser";
        RemoteCode remoteCode = redisRemoteCodeService.generateCodeAndSubscribe(mockFirstPrincipal.getName(), hostNickname);

        int maxMember = redisRemoteCodeService.getMaxChannelMember();

        // when
        // memberUser1 은 hostUser 에 해당하므로 2부터 시작
        for (int i = 2; i <= maxMember; i++) {
            String memberPrincipalName = "memberUser" + i;
            String memberNickname = "memberNickname" + i;
            redisRemoteCodeService.addSubscriber(remoteCode, memberPrincipalName, memberNickname);
        }

        // then
        String exceedMemberPrincipalName = "exceedMember";
        String exceedMemberNickname = "exceedNickname";
        // 예외 캡처 및 로깅
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            redisRemoteCodeService.addSubscriber(remoteCode, exceedMemberPrincipalName, exceedMemberNickname);
        });
        // 로그에 에러 메시지 출력
        log.info("Caught exception: {}", exception.getMessage());
    }
}
