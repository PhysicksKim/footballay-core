package com.footballay.core.config;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
        String gyeDomain = appEnvironmentVariable.getGYE_DOMAIN();
        String footballayDomain = appEnvironmentVariable.getFOOTBALLAY_DOMAIN();

        // then
        log.info("gyechunhoe domain : {}", gyeDomain);
        log.info("footballay domain : {}", footballayDomain);
        assertThat(StringUtils.hasText(gyeDomain)).isTrue();
        assertThat(StringUtils.hasText(footballayDomain)).isTrue();
    }
}