package com.gyechunsik.scoreboard.config;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@SpringBootTest
class CustomEnvironmentVariableTest {

    @Autowired
    private CustomEnvironmentVariable customEnvironmentVariable;

    @DisplayName("환경변수 값을 가져옴")
    @Test
    void getEnvVal() {
        // given

        // when
        String mainDomain = customEnvironmentVariable.getMainDomain();

        // then
        log.info("mainDomain: {}", mainDomain);
        assertThat(StringUtils.hasText(mainDomain)).isTrue();
    }
}