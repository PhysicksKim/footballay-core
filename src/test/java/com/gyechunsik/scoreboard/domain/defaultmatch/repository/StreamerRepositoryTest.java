package com.gyechunsik.scoreboard.domain.defaultmatch.repository;

import com.gyechunsik.scoreboard.domain.defaultmatch.entity.Streamer;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DataJpaTest
class StreamerRepositoryTest {

    @Autowired
    private StreamerRepository repository;

    @Autowired
    private EntityManager em;

    @DisplayName("Streamer 저장 시 Hash 값으로 저장합니다")
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
        log.info("스트리머 hash : {}", hash);
    }

}