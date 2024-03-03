package com.gyechunsik.scoreboard.websocket.domain.autoremote;

import com.gyechunsik.scoreboard.config.AbstractRedisTestContainerInit;
import com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.AutoRemote;
import com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.service.AnonymousUserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;

import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
class AutoRemoteTest extends AbstractRedisTestContainerInit {

    @Autowired
    private AutoRemote autoRemote;

    @Mock
    private AnonymousUserService anonymousUserService;

    // private final AutoRemoteGroupService autoRemoteGroupService;
    // private final AutoRemoteService autoRemoteService;

    @Mock
    private Principal mockFirstPrincipal;

    @Mock
    private Principal mockSecondPrincipal;

    private static final String SUFFIX_MOCK_PRINCIPAL_NAME = "THIS_WILL_BE_JSESSIONID";
    private static final String FIRST_USER_PRINCIPAL_NAME = "first_" + SUFFIX_MOCK_PRINCIPAL_NAME;
    private static final String SECOND_USER_PRINCIPAL_NAME = "second_" + SUFFIX_MOCK_PRINCIPAL_NAME;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Mockito를 사용하여 목 초기화
        when(mockFirstPrincipal.getName()).thenReturn(FIRST_USER_PRINCIPAL_NAME);
        when(mockSecondPrincipal.getName()).thenReturn(SECOND_USER_PRINCIPAL_NAME);
    }

    // TODO : AutoRemote 도메인에 대한 테스트 작성 필요
    /**
     * {@link AutoRemote#cacheUserBeforeAutoRemote(Principal, String)}
     */
    @Transactional
    @DisplayName("pre-remote cache 요청에 성공합니다.")
    @Test
    void preRemoteCache_Success() {
        // given
        autoRemote.connect(mockFirstPrincipal, "nickname");
        // FAIL BECAUSE OF NO IMPLEMENTATION
        // TODO : implement function for persist current RemoteCode Group to AutoRemoteGroup

        // when

        // then
    }


}