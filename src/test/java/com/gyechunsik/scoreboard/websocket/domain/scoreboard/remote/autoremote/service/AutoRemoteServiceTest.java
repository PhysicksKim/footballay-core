package com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.service;

import com.gyechunsik.scoreboard.config.AbstractRedisTestContainerInit;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.entity.AnonymousUser;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.entity.AutoRemoteGroup;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.repository.AutoRemoteRedisRepository;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.autoremote.service.AutoRemoteService;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.service.RedisRemoteCodeService;
import com.gyechunsik.scoreboard.websocket.domain.scoreboard.remote.code.RemoteCode;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@Slf4j
@ActiveProfiles("mockapi")
@Transactional
@SpringBootTest
class AutoRemoteServiceTest extends AbstractRedisTestContainerInit {

    @Autowired
    private RedisRemoteCodeService redisRemoteCodeService;
    @Autowired
    private AutoRemoteService autoRemoteService;

    @Autowired
    private AutoRemoteRedisRepository autoRemoteRedisRepository;
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

    private static final String SUFFIX_MOCK_PRINCIPAL_NAME = "THIS_WILL_BE_JSESSIONID_autoremoteservicetest";
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
        AutoRemoteGroup autoRemoteGroup = autoRemoteService.createAutoRemoteGroup();
        AnonymousUser firstSavedUser = autoRemoteService.createAndSaveAnonymousUser(autoRemoteGroup);
        UUID userUUID = firstSavedUser.getId();
        // 비활성 상태로 변경하기 위한 테스트용 메서드 호출
        // autoRemoteService.
        autoRemoteRedisRepository.removeAllActiveGroups();

        // when
        autoRemoteService.validateAndCacheUserToRedis(mockFirstPrincipal, userUUID.toString());
        RemoteCode remoteCode = autoRemoteService.connectToPrevFormedAutoRemoteGroup(mockFirstPrincipal, "nickname");

        // then
        // 새로운 코드 활성 여부 체크
        Set<String> getAllRemoteCodes = ReflectionTestUtils.invokeMethod(redisRemoteCodeService, "getAllRemoteCodes");
        String remoteCodeSetPrefix = (String) ReflectionTestUtils.getField(redisRemoteCodeService, "REMOTECODE_SET_PREFIX");
        assertThat(remoteCode).isNotNull();
        assertThat(getAllRemoteCodes).isNotEmpty();
        assertThat(getAllRemoteCodes).contains(remoteCodeSetPrefix + remoteCode.getRemoteCode());

        // 자동 원격 그룹 활성 체크
        String keyToGetRemoteCode = ReflectionTestUtils.invokeMethod(AutoRemoteRedisRepository.class, "activeKeyFromGroup", autoRemoteGroup.getId().toString());
        String keyToGetGroupId = ReflectionTestUtils.invokeMethod(AutoRemoteRedisRepository.class, "activeKeyFromCode", remoteCode.getRemoteCode());
        log.info("Active Auto Remote Key Pair: {} - {}", keyToGetRemoteCode, keyToGetGroupId);

        Map<String, String> activeAutoRemoteGroups = autoRemoteRedisRepository.getAllActiveGroups();
        log.info("map : {}", activeAutoRemoteGroups);
        assertThat(activeAutoRemoteGroups).isNotEmpty();
        assertThat(activeAutoRemoteGroups).containsKeys(keyToGetRemoteCode, keyToGetGroupId);
        assertThat(activeAutoRemoteGroups.get(keyToGetRemoteCode)).isEqualTo(remoteCode.getRemoteCode());

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
        AutoRemoteGroup autoRemoteGroup = autoRemoteService.createAutoRemoteGroup();
        AnonymousUser firstSavedUser = autoRemoteService.createAndSaveAnonymousUser(autoRemoteGroup);
        AnonymousUser secondSavedUser = autoRemoteService.createAndSaveAnonymousUser(autoRemoteGroup);
        UUID firstUUID = firstSavedUser.getId();
        UUID secondUUID = secondSavedUser.getId();

        // "유저1" 에 의해 원격 그룹 활성화
        autoRemoteService.validateAndCacheUserToRedis(mockFirstPrincipal, firstUUID.toString());
        RemoteCode firstConnectRemoteCode = autoRemoteService.connectToPrevFormedAutoRemoteGroup(mockFirstPrincipal, FIRST_USER_NICKNAME);

        // when
        autoRemoteService.validateAndCacheUserToRedis(mockSecondPrincipal, secondUUID.toString());
        RemoteCode secondConnectRemoteCode = autoRemoteService.connectToPrevFormedAutoRemoteGroup(mockSecondPrincipal, SECOND_USER_NICKNAME);

        // then
        // 새로운 코드 활성 여부 체크
        Set<String> getAllRemoteCodes = ReflectionTestUtils.invokeMethod(redisRemoteCodeService, "getAllRemoteCodes");
        String remoteCodeSetPrefix = (String) ReflectionTestUtils.getField(redisRemoteCodeService, "REMOTECODE_SET_PREFIX");
        assertThat(firstConnectRemoteCode).isNotNull();
        assertThat(getAllRemoteCodes).isNotEmpty();
        assertThat(getAllRemoteCodes).contains(remoteCodeSetPrefix + firstConnectRemoteCode.getRemoteCode());
        assertThat(firstConnectRemoteCode).isEqualTo(secondConnectRemoteCode);

        // Key Pair
        String keyToGetRemoteCode = ReflectionTestUtils.invokeMethod(AutoRemoteRedisRepository.class, "activeKeyFromGroup", autoRemoteGroup.getId().toString());
        String keyToGetGroupId = ReflectionTestUtils.invokeMethod(AutoRemoteRedisRepository.class, "activeKeyFromCode", firstConnectRemoteCode.getRemoteCode());
        log.info("Active Auto Remote Key Pair: {} - {}", keyToGetRemoteCode, keyToGetGroupId);
        log.info("First and Second RemoteCode : {} and {}", firstConnectRemoteCode.getRemoteCode(), secondConnectRemoteCode.getRemoteCode());

        // 활성화된 자동 원격 그룹 체크
        Map<String, String> activeAutoRemoteGroups = autoRemoteRedisRepository.getAllActiveGroups();
        log.info("map : {}", activeAutoRemoteGroups);
        assertThat(activeAutoRemoteGroups).isNotEmpty();
        assertThat(activeAutoRemoteGroups).containsKeys(keyToGetGroupId, keyToGetRemoteCode);
        assertThat(activeAutoRemoteGroups.get(keyToGetRemoteCode)).isEqualTo(secondConnectRemoteCode.getRemoteCode());

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
        AutoRemoteGroup autoRemoteGroup = autoRemoteService.createAutoRemoteGroup();
        AnonymousUser firstSavedUser = autoRemoteService.createAndSaveAnonymousUser(autoRemoteGroup);
        UUID userUUID = firstSavedUser.getId();
        // 비활성 상태로 변경하기 위한 테스트용 메서드 호출
        autoRemoteRedisRepository.removeAllActiveGroups();

        // when
        log.info("UUID NOT CACHED!!");
        log.info("does not called :: validateAndCacheUserToRedis() method");
        // autoRemoteService.validateAndCacheUserToRedis(mockFirstPrincipal, userUUID.toString());

        // then
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> autoRemoteService.connectToPrevFormedAutoRemoteGroup(mockFirstPrincipal, "nickname"),
                "잘못된 요청입니다. 사용자 UUID 또는 Principal 이 존재하지 않습니다."
        );
    }

    @Transactional
    @DisplayName("일치하는 익명 유저가 없는 경우, IllegalArgumentException 을 반환한다.")
    @Test
    void Fail_AnonymousUserNotExist() {
        // given
        AutoRemoteGroup autoRemoteGroup = autoRemoteService.createAutoRemoteGroup();
        AnonymousUser firstSavedUser = autoRemoteService.createAndSaveAnonymousUser(autoRemoteGroup);
        AnonymousUser secondSavedUser = autoRemoteService.createAndSaveAnonymousUser(autoRemoteGroup);
        UUID firstSavedUserId = firstSavedUser.getId();
        UUID secondSavedUserId = secondSavedUser.getId();
        // 비활성 상태로 변경하기 위한 테스트용 메서드 호출
        autoRemoteRedisRepository.removeAllActiveGroups();

        // when & then
        autoRemoteService.validateAndCacheUserToRedis(mockFirstPrincipal, firstSavedUserId.toString());

        // then
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> autoRemoteService.connectToPrevFormedAutoRemoteGroup(mockSecondPrincipal, "nickname"),
                "존재하지 않는 익명 유저입니다."
        );
    }
}