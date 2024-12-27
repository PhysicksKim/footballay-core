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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

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
    private EntityManager testEntityManager;

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

        testEntityManager.persist(player1);
        testEntityManager.persist(player2);
        testEntityManager.persist(player3);

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

        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Nested
    @DisplayName("findActivePhotosByPreferenceKeyAndPlayers 메서드 테스트")
    class FindActivePhotosByPreferenceKeyAndPlayersTest {

        @Test
        @DisplayName("활성화된 커스텀 사진들을 잘 가져오는지 테스트 (새로운 구조 - userFilePath, fileName)")
        void testFindActivePhotos() {
            // given
            PlayerCustomPhoto photo1 = PlayerCustomPhoto.builder()
                    .preferenceKey(preferenceKey)
                    .player(player1)
                    .userFilePath(userFilePath)
                    .fileName("photo1.png")
                    .version(1)
                    .isActive(true)
                    .build();

            PlayerCustomPhoto photo2 = PlayerCustomPhoto.builder()
                    .preferenceKey(preferenceKey)
                    .player(player2)
                    .userFilePath(userFilePath)
                    .fileName("photo2.png")
                    .version(1)
                    .isActive(true)
                    .build();

            // 비활성화된 사진
            PlayerCustomPhoto photo3 = PlayerCustomPhoto.builder()
                    .preferenceKey(preferenceKey)
                    .player(player1)
                    .userFilePath(userFilePath)
                    .fileName("photo3.png")
                    .version(2)
                    .isActive(false)
                    .build();

            // 다른 PreferenceKey
            PlayerCustomPhoto photo4 = PlayerCustomPhoto.builder()
                    .preferenceKey(preferenceKey2)
                    .player(player1)
                    .userFilePath(userFilePath)
                    .fileName("photo4.png")
                    .version(1)
                    .isActive(true)
                    .build();

            playerCustomPhotoRepository.save(photo1);
            playerCustomPhotoRepository.save(photo2);
            playerCustomPhotoRepository.save(photo3);
            playerCustomPhotoRepository.save(photo4);
            testEntityManager.flush();

            // when
            List<PlayerCustomPhoto> result = playerCustomPhotoRepository.findActivePhotosByPreferenceKeyAndPlayers(
                    preferenceKey.getId(),
                    Set.of(player1.getId(), player2.getId(), player3.getId())
            );

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting("fileName").containsExactlyInAnyOrder("photo1.png", "photo2.png");

            // photo1, photo2 활성, 같은 preferenceKey
            // photo3 비활성
            // photo4 다른 preferenceKey
        }
    }

    @Nested
    @DisplayName("findLatestPhotoByPreferenceKeyAndPlayer 메서드 테스트")
    class FindLatestPhotoByPreferenceKeyAndPlayerTest {

        @Test
        @DisplayName("version이 가장 큰 사진을 잘 가져오는지 테스트 (새로운 구조)")
        void testFindLatestPhoto() {
            // given
            PlayerCustomPhoto photo1 = PlayerCustomPhoto.builder()
                    .preferenceKey(preferenceKey)
                    .player(player1)
                    .userFilePath(userFilePath)
                    .fileName("photo1.png")
                    .version(1)
                    .isActive(true)
                    .build();

            PlayerCustomPhoto photo2 = PlayerCustomPhoto.builder()
                    .preferenceKey(preferenceKey)
                    .player(player1)
                    .userFilePath(userFilePath)
                    .fileName("photo2.png")
                    .version(2)
                    .isActive(true)
                    .build();

            // version이 더 크지만 isActive=false
            PlayerCustomPhoto photo3 = PlayerCustomPhoto.builder()
                    .preferenceKey(preferenceKey)
                    .player(player1)
                    .userFilePath(userFilePath)
                    .fileName("photo3.png")
                    .version(3)
                    .isActive(false)
                    .build();

            PlayerCustomPhoto photo4 = PlayerCustomPhoto.builder()
                    .preferenceKey(preferenceKey)
                    .player(player2)
                    .userFilePath(userFilePath)
                    .fileName("photo4.png")
                    .version(10)
                    .isActive(true)
                    .build();

            playerCustomPhotoRepository.save(photo1);
            playerCustomPhotoRepository.save(photo2);
            playerCustomPhotoRepository.save(photo3);
            playerCustomPhotoRepository.save(photo4);
            testEntityManager.flush();

            // when
            Optional<PlayerCustomPhoto> latest = playerCustomPhotoRepository.findLatestPhotoByPreferenceKeyAndPlayer(
                    preferenceKey.getId(),
                    player1.getId()
            );

            // then
            assertThat(latest).isPresent();
            assertThat(latest.get().getVersion()).isEqualTo(3);  // version이 가장 큰 값
            assertThat(latest.get().getFileName()).isEqualTo("photo3.png");

            // getPhotoUrl 활용 예시
            // Lazy 로딩 테스트 (userFilePath)
            String photoUrl = latest.get().getPhotoUrl();
            log.info("PhotoUrl = {}", photoUrl);
            assertThat(photoUrl).contains("photo3.png");
        }

        @Test
        @DisplayName("해당 데이터가 없으면 Optional.empty 반환")
        void testFindLatestPhoto_Empty() {
            // given - 아무 데이터 없음

            // when
            Optional<PlayerCustomPhoto> latest = playerCustomPhotoRepository.findLatestPhotoByPreferenceKeyAndPlayer(
                    preferenceKey.getId(),
                    player1.getId()
            );

            // then
            assertThat(latest).isEmpty();
        }
    }

    @Nested
    @DisplayName("findActivePhotosByPreferenceKeyAndPlayer 메서드 테스트")
    class FindActivePhotosByPreferenceKeyAndPlayerTest {

        @Test
        @DisplayName("isActive=true인 사진들만 가져오는지 테스트 (새로운 구조)")
        void testFindActivePhotos() {
            // given
            PlayerCustomPhoto photo1 = PlayerCustomPhoto.builder()
                    .preferenceKey(preferenceKey)
                    .player(player1)
                    .userFilePath(userFilePath)
                    .fileName("photo1.png")
                    .version(1)
                    .isActive(true)
                    .build();

            PlayerCustomPhoto photo2 = PlayerCustomPhoto.builder()
                    .preferenceKey(preferenceKey)
                    .player(player1)
                    .userFilePath(userFilePath)
                    .fileName("photo2.png")
                    .version(2)
                    .isActive(false) // 비활성
                    .build();

            PlayerCustomPhoto photo3 = PlayerCustomPhoto.builder()
                    .preferenceKey(preferenceKey)
                    .player(player2)
                    .userFilePath(userFilePath)
                    .fileName("photo3.png")
                    .version(1)
                    .isActive(true)  // 다른 player
                    .build();

            playerCustomPhotoRepository.save(photo1);
            playerCustomPhotoRepository.save(photo2);
            playerCustomPhotoRepository.save(photo3);
            testEntityManager.flush();

            // when
            List<PlayerCustomPhoto> result = playerCustomPhotoRepository.findActivePhotosByPreferenceKeyAndPlayer(
                    preferenceKey.getId(),
                    player1.getId()
            );

            // then
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getFileName()).isEqualTo("photo1.png");

            // getPhotoUrl 활용 예시
            String photoUrl = result.get(0).getPhotoUrl();
            log.info("Active PhotoUrl = {}", photoUrl);
            assertThat(photoUrl).contains("photo1.png");
        }

        @Test
        @DisplayName("활성화된 사진이 없으면 빈 리스트 반환")
        void testFindActivePhotos_Empty() {
            // given
            PlayerCustomPhoto photo1 = PlayerCustomPhoto.builder()
                    .preferenceKey(preferenceKey)
                    .player(player1)
                    .userFilePath(userFilePath)
                    .fileName("photo1.png")
                    .version(1)
                    .isActive(false)
                    .build();

            playerCustomPhotoRepository.save(photo1);
            testEntityManager.flush();

            // when
            List<PlayerCustomPhoto> result = playerCustomPhotoRepository.findActivePhotosByPreferenceKeyAndPlayer(
                    preferenceKey.getId(),
                    player1.getId()
            );

            // then
            assertThat(result).isEmpty();
        }
    }
}