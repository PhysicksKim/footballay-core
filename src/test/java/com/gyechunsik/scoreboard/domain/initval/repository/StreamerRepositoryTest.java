package com.gyechunsik.scoreboard.domain.initval.repository;

import com.gyechunsik.scoreboard.domain.initval.Entity.Streamer;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@DataJpaTest
class StreamerRepositoryTest {

    // todo : hash 저장해두고 em.clear() 후에 다시 찾는 테스트
    @Autowired
    private StreamerRepository repository;

    @Autowired
    private EntityManager em;

    @DisplayName("")
    @Test
    void hashFind() {
        // given
        String name = "gye";
        Streamer streamer = new Streamer(name);
        Streamer save = repository.save(streamer);

        // when
        log.info("saved streamer :: {}", streamer);
        String hash = save.getHash();
        em.flush();
        em.clear();

        Streamer findStreamer = repository.findByHash(hash)
                .orElseThrow(() -> new RuntimeException("hash 로 찾았으나 일치하는 유저가 없습니다."));

        // then
        assertThat(hash).isEqualTo(findStreamer.getHash());
        assertThat(save).isEqualTo(findStreamer);

    }

}