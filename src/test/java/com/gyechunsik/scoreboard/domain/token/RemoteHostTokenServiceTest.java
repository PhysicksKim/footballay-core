package com.gyechunsik.scoreboard.domain.token;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@ActiveProfiles("mockapi")
@Transactional
@SpringBootTest
class RemoteHostTokenServiceTest {

    private static final String REMOTE_CODE = "2ro23p";

    @Autowired
    private RemoteHostTokenService remoteHostTokenService;

    @DisplayName("원격 코드를 바탕으로 토큰을 생성 후 검증합니다")
    @Test
    void generateRemoteHostTokenAndValidate() {
        // given
        String token = makeToken(REMOTE_CODE, LocalDateTime.now());

        // when
        boolean isTokenValid = remoteHostTokenService.validateRemoteHostToken(token, REMOTE_CODE);

        // then
        assertThat(isTokenValid).isTrue();
    }

    @DisplayName("원격 코드가 일치하지 않는 토큰을 검증합니다")
    @Test
    void validateRemoteCodeMismatch() {
        // given
        String token = makeToken(REMOTE_CODE, LocalDateTime.now());
        final String INVALID_REMOTE_CODE = "invalidCode";

        // when
        boolean isTokenValid = remoteHostTokenService.validateRemoteHostToken(token, INVALID_REMOTE_CODE);

        // then
        assertThat(isTokenValid).isFalse();
    }

    private String makeToken(String remoteCode, LocalDateTime generatedTime) {
        return remoteHostTokenService.generateRemoteHostToken(remoteCode, generatedTime);
    }

}