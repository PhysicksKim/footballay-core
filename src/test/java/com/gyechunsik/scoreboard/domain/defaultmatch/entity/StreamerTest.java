package com.gyechunsik.scoreboard.domain.defaultmatch.entity;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DataJpaTest
class StreamerTest {

    @Autowired
    private EntityManager em;

    @DisplayName("Entity 저장 시 ")
    @Test
    void HashAndCreatedAt() {
        // given
        String streamerName = "gyechunhoe";
        // a745845143ed7edf126617b14a7f567176c8fff9825e3601fe507331e0fde5d7

        // when
        Streamer streamer = new Streamer(streamerName);
        em.persist(streamer);

        // then
        assertThat(streamer.getHash()).isNotNull();
        assertThat(streamer.getCreatedAt()).isNotNull();
        assertThat(streamer.getId()).isNotNull();
    }

    @DisplayName("동일한 input 이 주어지는 경우 동일한 해시 값을 생성합니다")
    @Test
    void ToLearn_HashTest() {
        // given
        MessageDigest instance = null;
        try {
            instance = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
        String name = "exampleName";
        // when

        byte[] digest1 = instance.digest(name.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb1 = new StringBuilder();
        for (byte b : digest1) {
            sb1.append(String.format("%02x", b & 0xff));
        }
        byte[] digest2 = instance.digest(name.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb2 = new StringBuilder();
        for (byte b : digest2) {
            sb2.append(String.format("%02x", b & 0xff));
        }

        // then
        log.info("sb 1 : {}", sb1);
        log.info("sb 2 : {}", sb2);
        assertThat(sb1.toString()).isEqualTo(sb2.toString());
    }
}