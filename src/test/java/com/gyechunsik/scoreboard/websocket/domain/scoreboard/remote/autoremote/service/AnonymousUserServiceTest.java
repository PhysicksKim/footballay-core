package com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.service;

import com.gyechunsik.scoreboard.config.AbstractRedisTestContainerInit;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.entity.AnonymousUser;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.entity.AutoRemoteGroup;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.repository.AutoRemoteRedisRepository;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.service.AutoRemoteService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@Slf4j
@ActiveProfiles("mockapi")
@SpringBootTest
class AnonymousUserServiceTest extends AbstractRedisTestContainerInit {

    @Autowired
    private EntityManager em;

    @Autowired
    private AutoRemoteService autoRemoteService;
    @Autowired
    private AutoRemoteRedisRepository autoRemoteRedisRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private Principal mockPrincipal;

    private static final String MOCK_PRINCIPAL_NAME = "THIS_WILL_BE_JSESSIONID_userservicetest";

    @BeforeEach
    void setUp() {
        when(mockPrincipal.getName()).thenReturn(MOCK_PRINCIPAL_NAME);
    }
    @AfterEach
    void cleanUp() {
        stringRedisTemplate.delete(stringRedisTemplate.keys("*"));
    }

    @Transactional
    @DisplayName("유저 생성 및 저장 테스트")
    @Test
    void CreateAndSaveAnonymousUser() {
        // given
        AutoRemoteGroup autoRemoteGroup = persistAutoRemoteGroup();

        // when
        AnonymousUser savedUser = autoRemoteService.createAndSaveAnonymousUser(autoRemoteGroup);
        log.info("savedUser: {}", savedUser);

        // then
        Assertions.assertThat(savedUser).isNotNull();
        Assertions.assertThat(savedUser.getId()).isNotNull();
        Assertions.assertThat(savedUser.getAutoRemoteGroup()).isEqualTo(autoRemoteGroup);
        Assertions.assertThat(savedUser.getLastConnectedAt()).isNotNull();
    }

    @Transactional
    @DisplayName("유저 UUID Redis 캐싱 테스트. 캐싱 전에 유저가 유효한지 검증합니다.")
    @Test
    void UserValidatePassAndCache() {
        // given
        AutoRemoteGroup autoRemoteGroup = persistAutoRemoteGroup();
        AnonymousUser savedUser = autoRemoteService.createAndSaveAnonymousUser(autoRemoteGroup);
        log.info("savedUser: {}", savedUser);
        em.flush();
        em.clear();

        final String userUuid = savedUser.getId().toString();
        log.info("userUuid: {}", userUuid);

        // when
        autoRemoteService.validateAndCacheUserToRedis(mockPrincipal, userUuid);
        String cachedUserKey = ReflectionTestUtils.
                invokeMethod(AutoRemoteRedisRepository.class, "keyForPrincipalToUuid", MOCK_PRINCIPAL_NAME);
        assert cachedUserKey != null;
        String cachedUserValue = stringRedisTemplate.opsForValue().get(cachedUserKey);

        // then
        Assertions.assertThat(cachedUserValue).isNotNull();
        Assertions.assertThat(cachedUserValue).isEqualTo(userUuid);
    }

    @Transactional
    @DisplayName("직전에 캐싱된 UUID 와 일치하지 않는 경우 예외가 발생한다.")
    @Test
    void UserValidateFail() {
        // given
        AutoRemoteGroup autoRemoteGroup = persistAutoRemoteGroup();
        AnonymousUser savedUser = autoRemoteService.createAndSaveAnonymousUser(autoRemoteGroup);
        log.info("savedUser: {}", savedUser);
        em.flush();
        em.clear();

        // when & then
        final String NOT_EXIST_UUID = UUID.randomUUID().toString();
        log.info("NOT_EXIST_UUID: {}", NOT_EXIST_UUID);

        // then
        Assertions.assertThatThrownBy(() -> {
            autoRemoteService.validateAndCacheUserToRedis(mockPrincipal, NOT_EXIST_UUID);
        }).isInstanceOf(IllegalArgumentException.class).hasMessage("noshow:존재하지 않는 익명 유저입니다.");
    }

    private AutoRemoteGroup persistAutoRemoteGroup() {
        AutoRemoteGroup autoRemoteGroup = new AutoRemoteGroup();
        autoRemoteGroup.setExpiredAt(LocalDateTime.now().plusDays(1));
        autoRemoteGroup.setLastActiveAt(LocalDateTime.now());
        em.persist(autoRemoteGroup);
        em.flush();
        em.clear();
        return autoRemoteGroup;
    }
}