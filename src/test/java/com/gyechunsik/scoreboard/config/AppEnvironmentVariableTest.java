package com.gyechunsik.scoreboard.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.StringUtils;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@ActiveProfiles("dev")
@SpringBootTest
class AppEnvironmentVariableTest {

    @Autowired
    private AppEnvironmentVariable appEnvironmentVariable;

    @DisplayName("환경변수 값을 가져옴")
    @Test
    void getEnvVal() {
        // when
        String mainDomain = appEnvironmentVariable.getDomain();

        // then
        log.info("mainDomain: {}", mainDomain);
        assertThat(StringUtils.hasText(mainDomain)).isTrue();
    }
}