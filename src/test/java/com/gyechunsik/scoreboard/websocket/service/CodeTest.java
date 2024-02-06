package com.gyechunsik.scoreboard.websocket.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
class CodeTest {

    @DisplayName("RemoteCode 클래스는 codeValue 필드만 동일하면 같은 객체로 판단한다.")
    @Test
    void CodeEquals() {
        // given
        RemoteCode remoteCode1 = new RemoteCode("code");
        RemoteCode remoteCode2 = new RemoteCode("code");

        // when
        boolean result = remoteCode1.equals(remoteCode2);

        // then
        assertTrue(result);
    }
}