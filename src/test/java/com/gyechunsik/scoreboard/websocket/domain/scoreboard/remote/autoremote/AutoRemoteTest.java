package com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote;

import com.gyechunsik.scoreboard.config.AbstractRedisTestContainerInit;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.AutoRemote;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
class AutoRemoteTest extends AbstractRedisTestContainerInit {

    @Mock
    private Principal mockFirstPrincipal;

    @Mock
    private Principal mockSecondPrincipal;

    private static final String SUFFIX_MOCK_PRINCIPAL_NAME = "THIS_WILL_BE_JSESSIONID_autoremotetest";
    private static final String FIRST_USER_PRINCIPAL_NAME = "first_" + SUFFIX_MOCK_PRINCIPAL_NAME;
    private static final String SECOND_USER_PRINCIPAL_NAME = "second_" + SUFFIX_MOCK_PRINCIPAL_NAME;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Mockito를 사용하여 목 초기화
        when(mockFirstPrincipal.getName()).thenReturn(FIRST_USER_PRINCIPAL_NAME);
        when(mockSecondPrincipal.getName()).thenReturn(SECOND_USER_PRINCIPAL_NAME);
    }

    /**
     * {@link AutoRemote#cacheUserPrincipalAndUuidForAutoRemote(Principal, String)}
     */
    @Transactional
    @DisplayName("pre-remote cache 요청에 성공합니다.")
    @Test
    @Disabled
    void preRemoteCache_Success() {
        // given
        // autoRemoteConnect.connectToPrevFormedRemoteGroup(mockFirstPrincipal, "nickname");
        // FAIL BECAUSE OF NO IMPLEMENTATION

        // when

        // then
    }


}