package com.gyechunsik.scoreboard.websocket.domain.autoremote.service;

import com.gyechunsik.scoreboard.config.AbstractRedisTestContainerInit;
import com.gyechunsik.scoreboard.websocket.domain.autoremote.entity.AnonymousUser;
import com.gyechunsik.scoreboard.websocket.domain.autoremote.entity.AutoRemoteGroup;
import com.gyechunsik.scoreboard.websocket.user.StompPrincipal;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
class AnonymousUserServiceTest extends AbstractRedisTestContainerInit {

    @Autowired
    private EntityManager em;

    @Autowired
    private AnonymousUserService anonymousUserService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @MockBean
    private Principal mockPrincipal;

    private static final String MOCK_PRINCIPAL_NAME = "testUser";

    @BeforeEach
    void setUp() {
        when(mockPrincipal.getName()).thenReturn(MOCK_PRINCIPAL_NAME);
    }

    @Transactional
    @DisplayName("유저 생성 및 저장 테스트")
    @Test
    void CreateAndSaveAnonymousUser() {
        // given
        AutoRemoteGroup autoRemoteGroup = persistAutoRemoteGroup();

        // when
        AnonymousUser savedUser = anonymousUserService.createAndSaveAnonymousUser(autoRemoteGroup);
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
        AnonymousUser savedUser = anonymousUserService.createAndSaveAnonymousUser(autoRemoteGroup);
        em.flush();
        em.clear();

        final String userUuid = savedUser.getId().toString();

        // when
        anonymousUserService.validateAndCacheUserToRedis(mockPrincipal, userUuid);

        // then
        String value = stringRedisTemplate.opsForValue().get(MOCK_PRINCIPAL_NAME);
        log.info("value: {}", value);
        Assertions.assertThat(value).isNotNull();
        Assertions.assertThat(value).isEqualTo(userUuid);
    }

    @Transactional
    @DisplayName("Redis 에 일치하는 익명 유저 UUID 가 없는 경우")
    @Test
    void UserValidateFail() {
        // given
        AutoRemoteGroup autoRemoteGroup = persistAutoRemoteGroup();
        AnonymousUser savedUser = anonymousUserService.createAndSaveAnonymousUser(autoRemoteGroup);
        em.flush();
        em.clear();

        // when & then
        final String NOT_EXIST_UUID = UUID.randomUUID().toString();

        // then
        Assertions.assertThatThrownBy(() -> {
            anonymousUserService.validateAndCacheUserToRedis(mockPrincipal, NOT_EXIST_UUID);
        }).isInstanceOf(IllegalArgumentException.class).hasMessage("존재하지 않는 익명 유저입니다.");
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