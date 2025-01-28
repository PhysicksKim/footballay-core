package com.gyechunsik.scoreboard.domain.football.preference;

import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.preference.dto.PlayerCustomPhotoDto;
import com.gyechunsik.scoreboard.domain.football.preference.persistence.PreferenceKey;
import com.gyechunsik.scoreboard.domain.football.preference.repository.PreferenceKeyRepository;
import com.gyechunsik.scoreboard.domain.football.preference.service.PlayerCustomPhotoService;
import com.gyechunsik.scoreboard.domain.football.preference.service.PreferenceKeyService;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.user.entity.User;
import com.gyechunsik.scoreboard.domain.user.repository.UserRepository;
import com.gyechunsik.scoreboard.domain.user.service.UserService;
import com.gyechunsik.scoreboard.util.CustomPhotoMultipartGenerator;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles("mockapi")
@SpringBootTest
@Transactional
class FootballPreferenceServiceTest {

    @Autowired
    private FootballPreferenceService footballPreferenceService;

    @Autowired
    private PlayerCustomPhotoService playerCustomPhotoService;
    @Autowired
    private PreferenceKeyService preferenceKeyService;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PreferenceKeyRepository preferenceKeyRepository;

    @Autowired
    private EntityManager em;

    private PreferenceKey preferenceKey;

    private static final String USERNAME = "user1";
    private static final long PLAYER1_ID = 1L;
    private static final long PLAYER2_ID = 2L;
    private static final long TEAM1_ID = 100L;
    private static final List<Player> PLAYER_LIST = new ArrayList<>();

    @SpyBean
    private PlayerRepository playerRepository;

    @BeforeEach
    void setUp() {
        // 테스트용 Player 생성
        Player player1 = Player.builder()
                .id(PLAYER1_ID)
                .number(7)
                .name("Ronaldo")
                .koreanName("호날두")
                .position("F")
                .photoUrl("https://defaultdomain.com/players/" + PLAYER1_ID + ".jpg")
                .build();

        Player player2 = Player.builder()
                .id(PLAYER2_ID)
                .number(10)
                .name("Messi")
                .koreanName("메시")
                .position("F")
                .photoUrl("https://defaultdomain.com/players/" + PLAYER2_ID + ".jpg")
                .build();

        PLAYER_LIST.add(playerRepository.save(player1));
        PLAYER_LIST.add(playerRepository.save(player2));

        // 테스트용 User 생성
        final String PASSWORD = "password1";
        User user = User.builder()
                .username(USERNAME)
                .password(PASSWORD)
                .enabled(true)
                .build();
        userRepository.save(user);
        preferenceKey = preferenceKeyService.generatePreferenceKeyForUser(user);

        // SecurityContext 에 인증 정보 추가
        // SecurityContext context = SecurityContextHolder.createEmptyContext();
        // SimpleGrantedAuthority authority = new SimpleGrantedAuthority(Role.ROLE_ADMIN.name());
        // Collection<? extends GrantedAuthority> authorities = List.of(authority);
        // UsernamePasswordAuthenticationToken authentication =
        //         new UsernamePasswordAuthenticationToken(USERNAME, PASSWORD, authorities);
        // authentication.eraseCredentials();
        // context.setAuthentication(authentication);
        // SecurityContextHolder.setContext(context);
    }

    @AfterEach
    void tearDown() {
        PLAYER_LIST.clear();
        preferenceKeyRepository.deleteAll();
        userRepository.deleteAll();
        playerRepository.deleteAll();
    }

    @Nested
    @DisplayName("savePlayerCustomPhoto 메서드")
    class SavePlayerCustomPhotoTest {

        @Test
        @DisplayName("정상적으로 커스텀 이미지를 저장하고 로그에 출력한다")
        void savePlayerCustomPhoto() {
            // given
            MultipartFile multipartFile = CustomPhotoMultipartGenerator.generate();

            // when
            footballPreferenceService.savePlayerCustomPhoto(USERNAME, PLAYER1_ID, multipartFile);

            em.flush();
            em.clear();

            // then
            User user = userService.findUser(USERNAME);
            List<PlayerCustomPhotoDto> allPhotos =
                    playerCustomPhotoService.getAllCustomPhotosWithUsername(user.getUsername(), PLAYER1_ID);

            assertThat(allPhotos).hasSize(1);
            assertThat(allPhotos.get(0).getPlayerId()).isEqualTo(PLAYER1_ID);
            assertThat(allPhotos.get(0).getIsActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("getSquadActiveCustomPhotos 메서드")
    class GetSquadActiveCustomPhotosTest {

        @Test
        @DisplayName("teamId 에 해당하는 선수들의 active 커스텀 이미지 URL 맵을 가져온다")
        void getSquadActiveCustomPhotos() {
            // given
            Mockito.doReturn(PLAYER_LIST).when(playerRepository).findAllByTeam(TEAM1_ID);
            User user = userService.findUser(USERNAME);
            MultipartFile file1 = CustomPhotoMultipartGenerator.generate();
            MultipartFile file2 = CustomPhotoMultipartGenerator.generate();

            playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, file1);
            playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER2_ID, file2);

            em.flush();
            em.clear();

            // when
            Map<Long, String> squadPhotoMap = footballPreferenceService.getSquadActiveCustomPhotos(USERNAME, TEAM1_ID);

            // then
            assertThat(squadPhotoMap).isNotEmpty();
            assertThat(squadPhotoMap).hasSize(2);
            assertThat(squadPhotoMap).containsKeys(PLAYER1_ID, PLAYER2_ID);
        }

        @Test
        @DisplayName("두 선수 중 한 명만 커스텀 이미지를 등록하면, 나머지 선수는 squadPhotoMap 에 포함되지 않는다")
        void getSquadActiveCustomPhotosWithOnePlayer() {
            // given
            Mockito.doReturn(PLAYER_LIST).when(playerRepository).findAllByTeam(TEAM1_ID);
            User user = userService.findUser(USERNAME);
            MultipartFile file1 = CustomPhotoMultipartGenerator.generate();

            playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, file1);

            em.flush();
            em.clear();

            // when
            Map<Long, String> squadPhotoMap = footballPreferenceService.getSquadActiveCustomPhotos(USERNAME, TEAM1_ID);

            // then
            assertThat(squadPhotoMap).isNotEmpty();
            assertThat(squadPhotoMap).hasSize(1);
            assertThat(squadPhotoMap).containsKeys(PLAYER1_ID);
        }

        @Test
        @DisplayName("커스텀 이미지를 deactivate 한 경우, squadPhotoMap 에 포함되지 않는다")
        void getSquadActiveCustomPhotosWithDeactivatedPhoto() {
            // given
            Mockito.doReturn(PLAYER_LIST).when(playerRepository).findAllByTeam(TEAM1_ID);
            User user = userService.findUser(USERNAME);
            MultipartFile file1 = CustomPhotoMultipartGenerator.generate();
            MultipartFile file2 = CustomPhotoMultipartGenerator.generate();

            PlayerCustomPhotoDto dto1 = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, file1);
            playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER2_ID, file2);

            em.flush();
            em.clear();

            // when
            footballPreferenceService.deactivatePhoto(USERNAME, dto1.getId());
            Map<Long, String> squadPhotoMap = footballPreferenceService.getSquadActiveCustomPhotos(USERNAME, TEAM1_ID);

            // then
            assertThat(squadPhotoMap).isNotEmpty();
            assertThat(squadPhotoMap).hasSize(1);
            assertThat(squadPhotoMap).containsKeys(PLAYER2_ID);
        }

    }

    @Nested
    @DisplayName("getCustomPhotoUrlsOfPlayers 메서드")
    class GetAllPlayerCustomPhotoUrlsTest {

        @Test
        @DisplayName("active 선수의 커스텀 이미지를 가져온다")
        void getAllPlayerCustomPhotoUrls() {
            // given
            final String keyHash = preferenceKey.getKeyhash();
            User user = userService.findUser(USERNAME);
            MultipartFile file1 = CustomPhotoMultipartGenerator.generate();
            MultipartFile file2 = CustomPhotoMultipartGenerator.generate();
            PlayerCustomPhotoDto photoDto1 = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, file1);
            PlayerCustomPhotoDto photoDto2 = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER2_ID, file2);
            em.flush();
            em.clear();

            // when
            Map<Long, String> photoUrlMap = footballPreferenceService.getCustomPhotoUrlsOfPlayers(keyHash, Set.of(PLAYER1_ID, PLAYER2_ID));

            // then
            assertThat(photoUrlMap).isNotEmpty();
            assertThat(photoUrlMap).hasSize(2);
            assertThat(photoUrlMap).containsKeys(PLAYER1_ID, PLAYER2_ID);
            assertThat(photoUrlMap.get(PLAYER1_ID)).isEqualTo(photoDto1.getPhotoUrl());
            assertThat(photoUrlMap.get(PLAYER2_ID)).isEqualTo(photoDto2.getPhotoUrl());
        }

        @Test
        @DisplayName("inactive 커스텀 이미지는 가져오지 않는다")
        void getAllPlayerCustomPhotoUrlsWithInactive() {
            // given
            final String keyHash = preferenceKey.getKeyhash();
            User user = userService.findUser(USERNAME);
            MultipartFile file1 = CustomPhotoMultipartGenerator.generate();
            MultipartFile file2 = CustomPhotoMultipartGenerator.generate();
            PlayerCustomPhotoDto photoDto1 = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, file1);
            PlayerCustomPhotoDto photoDto2 = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER2_ID, file2);
            em.flush();
            em.clear();

            // when
            footballPreferenceService.deactivatePhoto(USERNAME, photoDto1.getId());
            Map<Long, String> photoUrlMap = footballPreferenceService.getCustomPhotoUrlsOfPlayers(keyHash, Set.of(PLAYER1_ID, PLAYER2_ID));

            // then
            assertThat(photoUrlMap).isNotEmpty();
            assertThat(photoUrlMap).hasSize(1);
            assertThat(photoUrlMap).containsKeys(PLAYER2_ID);
        }
    }

    @Nested
    @DisplayName("getAllPhotosOfPlayerIncludeInactive 메서드")
    class GetAllPhotosOfPlayerIncludeInactiveTest {

        @Test
        @DisplayName("해당 player 의 모든 커스텀 이미지를 active/inactive 구분 없이 조회한다")
        void getAllPhotosOfPlayerIncludeInactive() {
            // given
            MultipartFile file1 = CustomPhotoMultipartGenerator.generate();
            MultipartFile file2 = CustomPhotoMultipartGenerator.generate();

            User user = userService.findUser(USERNAME);
            playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, file1);
            // 이전의 이미지는 자동으로 deactivate 된다
            playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, file2);
            em.flush();
            em.clear();

            // when
            List<PlayerCustomPhotoDto> allPhotos =
                    footballPreferenceService.getAllPhotosOfPlayerIncludeInactive(USERNAME, PLAYER1_ID);

            // then
            assertThat(allPhotos).hasSize(2);
        }
    }

    @Nested
    @DisplayName("activatePhoto 메서드")
    class ActivatePhotoTest {

        @Test
        @DisplayName("이미지가 정상적으로 활성화 되면 true 를 반환")
        void activatePhoto() {
            // given
            MultipartFile file1 = CustomPhotoMultipartGenerator.generate();
            User user = userService.findUser(USERNAME);
            PlayerCustomPhotoDto photoDto = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, file1);

            em.flush();
            em.clear();

            // when
            boolean result = footballPreferenceService.activatePhoto(USERNAME, photoDto.getId());

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("activatePhoto 중 예외가 발생하면 false 를 반환")
        void activatePhotoFailed() {
            // given
            // 존재하지 않는 photoId
            final long invalidPhotoId = 999999L;

            // when
            boolean result = footballPreferenceService.activatePhoto(USERNAME, invalidPhotoId);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("deactivatePhoto 메서드")
    class DeactivatePhotoTest {

        @Test
        @DisplayName("이미지를 정상적으로 비활성화 하면 true 를 반환")
        void deactivatePhoto() {
            // given
            MultipartFile file1 = CustomPhotoMultipartGenerator.generate();
            User user = userService.findUser(USERNAME);
            PlayerCustomPhotoDto photoDto = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, file1);

            em.flush();
            em.clear();

            // when
            boolean result = footballPreferenceService.deactivatePhoto(USERNAME, photoDto.getId());

            // then
            assertThat(result).isTrue();

            // 실제로 비활성화 되었는지 확인
            List<PlayerCustomPhotoDto> allPhotos =
                    playerCustomPhotoService.getAllCustomPhotosWithUsername(USERNAME, PLAYER1_ID);
            assertThat(allPhotos).hasSize(1);
            assertThat(allPhotos.get(0).getIsActive()).isFalse();
        }

        @Test
        @DisplayName("이미지가 없다면 deactivatePhoto 시 false 를 반환")
        void deactivatePhotoNotExisting() {
            // given
            final long notExistingPhotoId = 999999L;

            // when
            boolean result = footballPreferenceService.deactivatePhoto(USERNAME, notExistingPhotoId);

            // then
            assertThat(result).isFalse();
        }
    }

    @Nested
    class SwitchToDefaultPhotoTest {

        @Test
        @DisplayName("선수의 모든 커스텀 이미지를 비활성화한다")
        @WithMockUser(username = USERNAME, password = "password1", roles = "ADMIN")
        void switchToDefaultPhoto() {
            // given
            MultipartFile file1 = CustomPhotoMultipartGenerator.generate();
            MultipartFile file2 = CustomPhotoMultipartGenerator.generate();
            User user = userService.findUser(USERNAME);
            playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, file1);
            playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, file2);

            em.flush();
            em.clear();

            // when
            footballPreferenceService.switchToDefaultPhoto(PLAYER1_ID);

            // then
            List<PlayerCustomPhotoDto> allPhotos =
                    playerCustomPhotoService.getAllCustomPhotosWithUsername(USERNAME, PLAYER1_ID);
            assertThat(allPhotos).hasSize(2);
            assertThat(allPhotos).allMatch(photo -> !photo.getIsActive());
        }
    }
}
