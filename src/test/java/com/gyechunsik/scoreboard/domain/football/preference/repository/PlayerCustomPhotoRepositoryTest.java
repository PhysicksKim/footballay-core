package com.gyechunsik.scoreboard.domain.football.preference.repository;

import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.preference.persistence.PlayerCustomPhoto;
import com.gyechunsik.scoreboard.domain.football.preference.persistence.PreferenceKey;
import com.gyechunsik.scoreboard.domain.football.preference.persistence.UserFilePath;
import com.gyechunsik.scoreboard.domain.football.preference.persistence.UserPathCategory;
import com.gyechunsik.scoreboard.entity.user.User;
import com.gyechunsik.scoreboard.entity.user.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@DataJpaTest
@Transactional
class PlayerCustomPhotoRepositoryTest {

    @Autowired
    private PlayerCustomPhotoRepository playerCustomPhotoRepository;

    @Autowired
    private UserFilePathRepository userFilePathRepository;

    @Autowired
    private PreferenceKeyRepository preferenceKeyRepository;

    @Autowired
    private EntityManager em;

    @Autowired
    private UserRepository userRepository;

    private PreferenceKey preferenceKey;
    private PreferenceKey preferenceKey2;
    private Player player1;
    private Player player2;
    private Player player3;
    private UserFilePath userFilePath;

    @BeforeEach
    void setUp() {
        // User 생성
        User user1 = User.builder()
                .username("user1")
                .password("password1")
                .enabled(true)
                .build();

        User user2 = User.builder()
                .username("user2")
                .password("password2")
                .enabled(true)
                .build();

        userRepository.save(user1);
        userRepository.save(user2);

        // PreferenceKey 생성
        preferenceKey = PreferenceKey.builder()
                .keyhash("prefKey1")
                .user(user1)
                .build();

        preferenceKey2 = PreferenceKey.builder()
                .keyhash("prefKey2")
                .user(user2)
                .build();

        preferenceKeyRepository.save(preferenceKey);
        preferenceKeyRepository.save(preferenceKey2);

        // Player 엔티티 생성
        player1 = Player.builder()
                .id(100L)
                .name("Player 1")
                .build();

        player2 = Player.builder()
                .id(101L)
                .name("Player 2")
                .build();

        player3 = Player.builder()
                .id(102L)
                .name("Player 3")
                .build();

        em.persist(player1);
        em.persist(player2);
        em.persist(player3);

        // UserFilePath 생성
        userFilePath = UserFilePath.builder()
                .user(user1)
                .domain("https://example.com/")
                .prefixPath("/prefixPath/")
                .suffixPath("/suffixPath/")
                .userPathHash("userHashKey123")
                .userPathCategory(UserPathCategory.PLAYER_CUSTOM_PHOTO)
                .build();
        userFilePathRepository.save(userFilePath);

        em.flush();
        em.clear();
    }

    @Nested
    class FindActivePhotosByPreferenceKeyAndPlayers {

        @DisplayName("여러 선수의 활성화된 커스텀 이미지를 조회한다")
        @Test
        void findActivePhotosFromPlayerIdSet() {
            // given
            PlayerCustomPhoto playerCustomPhoto1 = createAndSavePlayerCustomPhoto(preferenceKey, player1);
            PlayerCustomPhoto playerCustomPhoto2 = createAndSavePlayerCustomPhoto(preferenceKey, player2);
            PlayerCustomPhoto playerCustomPhoto3 = createAndSavePlayerCustomPhoto(preferenceKey, player3);

            Set<Long> playerIds = Set.of(player1.getId(), player2.getId(), player3.getId());
            em.flush();
            em.clear();

            // when
            List<PlayerCustomPhoto> activePhotos = playerCustomPhotoRepository.findAllActivesByPreferenceKeyAndPlayers(preferenceKey.getId(), playerIds);

            // then
            log.info("activePhotos: {}", activePhotos);
            assertThat(activePhotos).contains(playerCustomPhoto1, playerCustomPhoto2, playerCustomPhoto3);
        }


        @DisplayName("여러 선수 커스텀 이미지 조회 중 비활성화된 커스텀 이미지는 조회하지 않는다")
        @Test
        void findActivePhotosFromPlayerIdSetWithInactivePhoto() {
            // given
            PlayerCustomPhoto playerCustomPhoto1 = createAndSavePlayerCustomPhoto(preferenceKey, player1);
            PlayerCustomPhoto playerCustomPhoto2 = createAndSavePlayerCustomPhoto(preferenceKey, player2);
            PlayerCustomPhoto playerCustomPhoto3 = createAndSavePlayerCustomPhoto(preferenceKey, player3);
            playerCustomPhoto3.setActive(false);

            Set<Long> playerIds = Set.of(player1.getId(), player2.getId(), player3.getId());
            em.flush();
            em.clear();

            // when
            List<PlayerCustomPhoto> activePhotos = playerCustomPhotoRepository.findAllActivesByPreferenceKeyAndPlayers(preferenceKey.getId(), playerIds);

            // then
            log.info("activePhotos: {}", activePhotos);
            assertThat(activePhotos).contains(playerCustomPhoto1, playerCustomPhoto2);
            assertThat(activePhotos).doesNotContain(playerCustomPhoto3);
        }
    }

    @Nested
    class FindActivePhotoByPreferenceKeyAndPlayer {

        @DisplayName("선수 한 명의 활성화된 커스텀 이미지를 조회한다")
        @Test
        void findActivePhoto() {
            // given
            PlayerCustomPhoto playerCustomPhoto1 = createAndSavePlayerCustomPhoto(preferenceKey, player1);

            em.flush();
            em.clear();

            // when
            PlayerCustomPhoto activePhoto = playerCustomPhotoRepository.findActivePhotoByPreferenceKeyAndPlayer(preferenceKey, player1.getId()).orElseThrow();

            // then
            log.info("activePhoto: {}", activePhoto);
            assertThat(activePhoto).isEqualTo(playerCustomPhoto1);
        }

        @DisplayName("활성화된 커스텀 이미지가 없으면 빈 Optional을 반환한다")
        @Test
        void findActivePhotoWithNoActivePhoto() {
            // given
            PlayerCustomPhoto playerCustomPhoto = createAndSavePlayerCustomPhoto(preferenceKey, player1);
            playerCustomPhoto.setActive(false);

            em.flush();
            em.clear();

            // when
            Optional<PlayerCustomPhoto> optionalPhoto = playerCustomPhotoRepository.findActivePhotoByPreferenceKeyAndPlayer(preferenceKey, player1.getId());

            // then
            log.info("optionalPhoto: {}", optionalPhoto);
            assertThat(optionalPhoto).isEmpty();
        }

    }

    @Nested
    class FindAllByPreferenceKeyAndPlayer {

        @DisplayName("선수 한 명의 모든 커스텀 이미지를 조회한다")
        @Test
        void findAllPhotos() {
            // given
            PlayerCustomPhoto playerCustomPhoto1 = createAndSavePlayerCustomPhoto(preferenceKey, player1);
            PlayerCustomPhoto playerCustomPhoto2 = createAndSavePlayerCustomPhoto(preferenceKey, player1);
            PlayerCustomPhoto playerCustomPhoto3 = createAndSavePlayerCustomPhoto(preferenceKey, player1);

            em.flush();
            em.clear();

            // when
            List<PlayerCustomPhoto> allPhotos = playerCustomPhotoRepository.findAllByPreferenceKeyAndPlayer(preferenceKey, player1.getId());

            // then
            log.info("allPhotos: {}", allPhotos);
            assertThat(allPhotos).contains(playerCustomPhoto1, playerCustomPhoto2, playerCustomPhoto3);
        }

        @DisplayName("선수 한 명의 모든 커스텀 이미지 조회 중 비활성화된 커스텀 이미지도 조회한다")
        @Test
        void findAllPhotosWithInactivePhoto() {
            // given
            PlayerCustomPhoto playerCustomPhoto1 = createAndSavePlayerCustomPhoto(preferenceKey, player1);
            PlayerCustomPhoto playerCustomPhoto2 = createAndSavePlayerCustomPhoto(preferenceKey, player1);
            PlayerCustomPhoto playerCustomPhoto3 = createAndSavePlayerCustomPhoto(preferenceKey, player1);
            playerCustomPhoto3.setActive(false);

            em.flush();
            em.clear();

            // when
            List<PlayerCustomPhoto> allPhotos = playerCustomPhotoRepository.findAllByPreferenceKeyAndPlayer(preferenceKey, player1.getId());

            // then
            log.info("allPhotos: {}", allPhotos);
            assertThat(allPhotos).contains(playerCustomPhoto1, playerCustomPhoto2, playerCustomPhoto3);
        }

        @DisplayName("다른 PreferenceKey로 저장된 커스텀 이미지는 조회하지 않는다")
        @Test
        void findAllPhotosWithDifferentPreferenceKey() {
            // given
            PlayerCustomPhoto playerCustomPhoto1 = createAndSavePlayerCustomPhoto(preferenceKey, player1);
            PlayerCustomPhoto playerCustomPhoto2 = createAndSavePlayerCustomPhoto(preferenceKey2, player1);

            em.flush();
            em.clear();

            // when
            List<PlayerCustomPhoto> allPhotos = playerCustomPhotoRepository.findAllByPreferenceKeyAndPlayer(preferenceKey, player1.getId());

            // then
            log.info("allPhotos: {}", allPhotos);
            assertThat(allPhotos).contains(playerCustomPhoto1);
            assertThat(allPhotos).doesNotContain(playerCustomPhoto2);
        }
    }

    private PlayerCustomPhoto createAndSavePlayerCustomPhoto(PreferenceKey preferenceKey, Player player) {
        PlayerCustomPhoto playerCustomPhoto = PlayerCustomPhoto.builder()
                .preferenceKey(preferenceKey)
                .player(player)
                .userFilePath(userFilePath)
                .fileName("player" + player.getId() + ".jpg")
                .isActive(true)
                .build();

        return playerCustomPhotoRepository.save(playerCustomPhoto);
    }

}