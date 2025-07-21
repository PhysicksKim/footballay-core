package com.footballay.core.websocket.domain.scoreboard.remote.code;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CodeTest {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CodeTest.class);

    @DisplayName("RemoteCode 클래스는 codeValue 필드만 동일하면 같은 객체로 판단한다.")
    @Test
    void CodeEquals() {
        // given
        RemoteCode remoteCode1 = RemoteCode.of("excode");
        RemoteCode remoteCode2 = RemoteCode.of("excode");
        // when
        boolean result = remoteCode1.equals(remoteCode2);
        // then
        assertTrue(result);
    }
}
