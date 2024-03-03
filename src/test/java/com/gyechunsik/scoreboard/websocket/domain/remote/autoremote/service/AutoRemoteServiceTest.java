package com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.service;

import com.gyechunsik.scoreboard.config.AbstractRedisTestContainerInit;
import com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.entity.AnonymousUser;
import com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.entity.AutoRemoteGroup;
import com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.service.AnonymousUserService;
import com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.service.AutoRemoteGroupService;
import com.gyechunsik.scoreboard.websocket.domain.remote.autoremote.service.AutoRemoteService;
import com.gyechunsik.scoreboard.websocket.domain.remote.code.RedisRemoteCodeService;
import com.gyechunsik.scoreboard.websocket.domain.remote.code.RemoteCode;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@Slf4j
@SpringBootTest
class AutoRemoteServiceTest extends AbstractRedisTestContainerInit {

    @Autowired
    private RedisRemoteCodeService redisRemoteCodeService;

    @Autowired
    private AutoRemoteService autoRemoteService;
    @Autowired
    private AnonymousUserService anonymousUserService;
    @Autowired
    private AutoRemoteGroupService autoRemoteGroupService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private EntityManager em;

    /**
     * 테스트 필요 분기들 정리
     * 1. SUCCESS : 자동 원격 채널이 생성되지 않은 경우
     * 2. SUCCESS : 자동 원격 채널이 생성되어 있는 경우
     * 3. FAIL : uuid 가 존재하지 않는 경우 (캐싱되지 않은 경우)
     * 4. FAIL : 일치하는 익명 유저가 없는 경우
     */

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

    @Transactional
    @DisplayName("비활성 상태인 자동 원격 그룹에 연결할 때, 자동 원격 그룹을 활성화 하고 새로운 사용자가 연결된다.")
    @Test
    void Success_connectAutoRemoteGroup_alreadyActivated() {
        // given
        AutoRemoteGroup autoRemoteGroup = autoRemoteGroupService.createAutoRemoteGroup();
        AnonymousUser firstSavedUser = anonymousUserService.createAndSaveAnonymousUser(autoRemoteGroup);
        UUID userUUID = firstSavedUser.getId();
        // 비활성 상태로 변경하기 위한 테스트용 메서드 호출
        // autoRemoteService.
        autoRemoteService.removeAllActivatedAutoRemoteGroups();

        // when
        anonymousUserService.validateAndCacheUserToRedis(mockFirstPrincipal, userUUID.toString());
        RemoteCode remoteCode = autoRemoteService.connect(mockFirstPrincipal, "nickname");

        // then
        // 새로운 코드 활성 여부 체크
        Set<String> getAllRemoteCodes = ReflectionTestUtils.invokeMethod(redisRemoteCodeService, "getAllRemoteCodes");
        String remoteCodeSetPrefix = (String) ReflectionTestUtils.getField(redisRemoteCodeService, "REMOTECODE_SET_PREFIX");
        assertThat(remoteCode).isNotNull();
        assertThat(getAllRemoteCodes).isNotEmpty();
        assertThat(getAllRemoteCodes).contains(remoteCodeSetPrefix + remoteCode.getRemoteCode());

        // 자동 원격 그룹 활성 체크
        Map<String, String> activeAutoRemoteGroups = autoRemoteService.getActiveAutoRemoteGroups();
        assertThat(activeAutoRemoteGroups).isNotEmpty();
        assertThat(activeAutoRemoteGroups).containsKey(autoRemoteGroup.getId().toString());
        assertThat(activeAutoRemoteGroups.get(autoRemoteGroup.getId().toString())).isEqualTo(remoteCode.getRemoteCode());

        // 로그 확인
        log.info("Active Remote codes : {}", getAllRemoteCodes);
        for (String getAllRemoteCode : getAllRemoteCodes) {
            log.info("remote Code subscribers : {}", stringRedisTemplate.opsForHash().entries(getAllRemoteCode));
        }
        log.info("Active AutoRemoteGroups = {key=autoRemoteGroupId, value=remoteCode} : {}", activeAutoRemoteGroups);
    }

    @Transactional
    @DisplayName("활성 상태인 자동 원격 그룹에 연결할 때, 기존 remoteCode 에 연결한다.")
    @Test
    void Success_connectAutoRemoteGroup_NotActivated() {
        // given
        final String FIRST_USER_NICKNAME = "firstUserNickname";
        final String SECOND_USER_NICKNAME = "secondUserNickname";

        // 자동 원격 그룹과 유저1, 유저2 생성
        AutoRemoteGroup autoRemoteGroup = autoRemoteGroupService.createAutoRemoteGroup();
        AnonymousUser firstSavedUser = anonymousUserService.createAndSaveAnonymousUser(autoRemoteGroup);
        AnonymousUser secondSavedUser = anonymousUserService.createAndSaveAnonymousUser(autoRemoteGroup);
        UUID firstUUID = firstSavedUser.getId();
        UUID secondUUID = secondSavedUser.getId();

        // "유저1" 에 의해 원격 그룹 활성화
        anonymousUserService.validateAndCacheUserToRedis(mockFirstPrincipal, firstUUID.toString());
        RemoteCode firstConnectRemoteCode = autoRemoteService.connect(mockFirstPrincipal, FIRST_USER_NICKNAME);

        // when
        anonymousUserService.validateAndCacheUserToRedis(mockSecondPrincipal, secondUUID.toString());
        RemoteCode secondConnectRemoteCode = autoRemoteService.connect(mockSecondPrincipal, SECOND_USER_NICKNAME);

        // then
        // 새로운 코드 활성 여부 체크
        Set<String> getAllRemoteCodes = ReflectionTestUtils.invokeMethod(redisRemoteCodeService, "getAllRemoteCodes");
        String remoteCodeSetPrefix = (String) ReflectionTestUtils.getField(redisRemoteCodeService, "REMOTECODE_SET_PREFIX");
        assertThat(firstConnectRemoteCode).isNotNull();
        assertThat(getAllRemoteCodes).isNotEmpty();
        assertThat(getAllRemoteCodes).contains(remoteCodeSetPrefix + firstConnectRemoteCode.getRemoteCode());
        assertThat(firstConnectRemoteCode).isEqualTo(secondConnectRemoteCode);

        // 활성화된 자동 원격 그룹 체크
        Map<String, String> activeAutoRemoteGroups = autoRemoteService.getActiveAutoRemoteGroups();
        assertThat(activeAutoRemoteGroups).isNotEmpty();
        assertThat(activeAutoRemoteGroups).containsKey(autoRemoteGroup.getId().toString());
        assertThat(activeAutoRemoteGroups.get(autoRemoteGroup.getId().toString())).isEqualTo(secondConnectRemoteCode.getRemoteCode());

        // 활성화된 자동 원격 그룹의 원격 코드로 구독자 체크
        Map<Object, Object> subscribers = redisRemoteCodeService.getSubscribers(firstConnectRemoteCode.getRemoteCode());
        assertThat(subscribers).isNotEmpty();
        assertThat(subscribers).containsKeys(FIRST_USER_PRINCIPAL_NAME, SECOND_USER_PRINCIPAL_NAME);
        assertThat(subscribers).containsValues(FIRST_USER_NICKNAME, SECOND_USER_NICKNAME);

        // 생성된 값들과 retrieved 값들을 로그로 확인
        log.info("Active Remote codes : {}", getAllRemoteCodes);
        for (String getAllRemoteCode : getAllRemoteCodes) {
            log.info("remote Code subscribers : {}", stringRedisTemplate.opsForHash().entries(getAllRemoteCode));
        }
        log.info("Active AutoRemoteGroups = {key=autoRemoteGroupId, value=remoteCode} : {}", activeAutoRemoteGroups);
    }

    @Transactional
    @DisplayName("UUID 가 캐싱되지 않은 경우, IllegalArgumentException 을 반환한다.")
    @Test
    void Fail_UUIDNotCached() {
        // given
        AutoRemoteGroup autoRemoteGroup = autoRemoteGroupService.createAutoRemoteGroup();
        AnonymousUser firstSavedUser = anonymousUserService.createAndSaveAnonymousUser(autoRemoteGroup);
        UUID userUUID = firstSavedUser.getId();
        // 비활성 상태로 변경하기 위한 테스트용 메서드 호출
        autoRemoteService.removeAllActivatedAutoRemoteGroups();

        // when
        log.info("UUID NOT CACHED!!");
        log.info("does not called :: validateAndCacheUserToRedis() method");
        // anonymousUserService.validateAndCacheUserToRedis(mockFirstPrincipal, userUUID.toString());

        // then
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> autoRemoteService.connect(mockFirstPrincipal, "nickname"),
                "잘못된 요청입니다. 사용자 UUID 또는 Principal 이 존재하지 않습니다."
        );
    }

    @Transactional
    @DisplayName("일치하는 익명 유저가 없는 경우, IllegalArgumentException 을 반환한다.")
    @Test
    void Fail_AnonymousUserNotExist() {
        // given
        AutoRemoteGroup autoRemoteGroup = autoRemoteGroupService.createAutoRemoteGroup();
        AnonymousUser firstSavedUser = anonymousUserService.createAndSaveAnonymousUser(autoRemoteGroup);
        AnonymousUser secondSavedUser = anonymousUserService.createAndSaveAnonymousUser(autoRemoteGroup);
        UUID firstSavedUserId = firstSavedUser.getId();
        UUID secondSavedUserId = secondSavedUser.getId();
        // 비활성 상태로 변경하기 위한 테스트용 메서드 호출
        autoRemoteService.removeAllActivatedAutoRemoteGroups();

        // when & then
        anonymousUserService.validateAndCacheUserToRedis(mockFirstPrincipal, firstSavedUserId.toString());

        // then
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> autoRemoteService.connect(mockSecondPrincipal, "nickname"),
                "존재하지 않는 익명 유저입니다."
        );
    }
}