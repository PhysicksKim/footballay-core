package com.gyechunsik.scoreboard.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DisplayName("패스워드 인코더 빈이 정상적으로 등록되어 있어야 한다")
    @Test
    void passwordEncoder() {
        String rawPassword = "asdf1234!";
        String encodedPassword = passwordEncoder.encode(rawPassword);
        log.info("rawPassword: {}, encodedPassword: {}", rawPassword, encodedPassword);
        assertTrue(passwordEncoder.matches(rawPassword, encodedPassword));
    }
}