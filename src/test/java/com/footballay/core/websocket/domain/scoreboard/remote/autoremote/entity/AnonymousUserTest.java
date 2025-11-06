package com.footballay.core.websocket.domain.scoreboard.remote.autoremote.entity;

import jakarta.persistence.EntityManager;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDateTime;
import java.util.UUID;

@DataJpaTest
class AnonymousUserTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AnonymousUserTest.class);
    @Autowired
    private EntityManager em;

    @DisplayName("String 을 UUID 객체로 변환합니다.")
    @Test
    void StringToUUIDConvert() {
        // given
        UUID uuid = UUID.randomUUID();
        log.info("uuid: {}", uuid);
        // when
        String uuidString = uuid.toString();
        UUID convertedUuid = UUID.fromString(uuidString);
        log.info("convertedUuid: {}", convertedUuid);
        // then
        Assertions.assertThat(convertedUuid.toString()).isEqualTo(uuidString);
    }

    @DisplayName("")
    @Test
    void generateAnonymousUser() {
        // given
        AutoRemoteGroup autoRemoteGroup = new AutoRemoteGroup();
        autoRemoteGroup.setExpiredAt(LocalDateTime.now().plusDays(1));
        autoRemoteGroup.setLastActiveAt(LocalDateTime.now());
        em.persist(autoRemoteGroup);
        AnonymousUser anonymousUser = new AnonymousUser();
        anonymousUser.setAutoRemoteGroup(autoRemoteGroup);
        anonymousUser.setLastConnectedAt(LocalDateTime.now());
        log.info("UUID before flush : {}", anonymousUser.getId());
        em.persist(anonymousUser);
        em.flush();
        em.clear();
        // when
        AnonymousUser foundAnonymousUser = em.find(AnonymousUser.class, anonymousUser.getId());
        UUID id = foundAnonymousUser.getId();
        AutoRemoteGroup fkRemoteGroup = foundAnonymousUser.getAutoRemoteGroup();
        // then
        Assertions.assertThat(foundAnonymousUser).isNotNull();
        Assertions.assertThat(fkRemoteGroup).isNotNull();
        Assertions.assertThat(id).isNotNull();
        log.info("fkRemoteGroup: {}", fkRemoteGroup);
        log.info("id: {}", id);
    }
}
