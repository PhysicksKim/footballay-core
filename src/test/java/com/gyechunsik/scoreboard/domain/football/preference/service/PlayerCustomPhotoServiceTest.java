package com.gyechunsik.scoreboard.domain.football.preference.service;

import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.preference.dto.PlayerCustomPhotoDto;
import com.gyechunsik.scoreboard.domain.football.preference.persistence.PlayerCustomPhoto;
import com.gyechunsik.scoreboard.domain.football.preference.persistence.PreferenceKey;
import com.gyechunsik.scoreboard.domain.football.preference.repository.PlayerCustomPhotoRepository;
import com.gyechunsik.scoreboard.domain.football.preference.repository.PreferenceKeyRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.user.entity.User;
import com.gyechunsik.scoreboard.domain.user.repository.UserRepository;
import com.gyechunsik.scoreboard.util.CustomPhotoMultipartGenerator;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@ActiveProfiles("mockapi")
@SpringBootTest
@Transactional
class PlayerCustomPhotoServiceTest {

    @Autowired
    private PlayerCustomPhotoService playerCustomPhotoService;
    @Autowired
    private PlayerRepository playerRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PreferenceKeyRepository preferenceKeyRepository;
    @Autowired
    private PlayerCustomPhotoRepository playerCustomPhotoRepository;
    @Autowired
    private PreferenceKeyService preferenceKeyService;
    @Autowired
    private EntityManager em;

    private PreferenceKey preferenceKey;

    private static final long PLAYER1_ID = 1L;
    private static final long PLAYER2_ID = 2L;
    private static final String USERNAME = "user1";

    @BeforeEach
    void setData() {
        // add player
        Player player1 = Player.builder()
                .id(PLAYER1_ID)
                .number(7)
                .name("Ronaldo")
                .photoUrl("https://defaultdomain.com/players/" + PLAYER1_ID + ".jpg")
                .koreanName("호날두")
                .position("F")
                .build();
        Player player2 = Player.builder()
                .id(PLAYER2_ID)
                .number(10)
                .name("Messi")
                .photoUrl("https://defaultdomain.com/players/" + PLAYER2_ID + ".jpg")
                .koreanName("메시")
                .position("F")
                .build();
        playerRepository.save(player1);
        playerRepository.save(player2);

        // add user
        User user = User.builder()
                .username(USERNAME)
                .password("password1")
                .enabled(true)
                .build();
        userRepository.save(user);
        preferenceKey = preferenceKeyService.generatePreferenceKeyForUser(user);
    }

    @AfterEach
    void tearDown() {
        playerCustomPhotoRepository.deleteAll();
        preferenceKeyRepository.deleteAll();
        userRepository.deleteAll();
        playerRepository.deleteAll();
    }

    @Nested
    class RegisterAndUploadCustomPhoto {

        @DisplayName("커스텀 선수 이미지를 등록하고 업로드한다")
        @Test
        void success_registerPlayerCustomPhoto() {
            // given
            MultipartFile multipartFile = CustomPhotoMultipartGenerator.generate();
            User user = userRepository.findByUsername(USERNAME).orElseThrow();
            log.info("preferenceKey: {}", preferenceKey.getKeyhash());

            // when
            PlayerCustomPhotoDto dto = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile);
            log.info("dto: {}", dto);

            // then
            assertThat(dto).isNotNull();
            assertThat(dto.getPhotoUrl()).isNotNull();
            assertThat(dto.getPlayerId()).isEqualTo(1L);
            assertThat(dto.getIsActive()).isTrue();
            assertThat(dto.getId()).isNotNull();
            assertThat(dto.getUpdatedAt()).isNotNull();
            assertThat(dto.getUploadedAt()).isNotNull();
        }

        @DisplayName("가장 최신에 업로드된 이미지가 활성화된다")
        @Test
        void registerTwoPhotoAndOnlyActiveLatest() {
            // given
            MultipartFile multipartFile1 = CustomPhotoMultipartGenerator.generate();
            MultipartFile multipartFile2 = CustomPhotoMultipartGenerator.generate();
            User user = userRepository.findByUsername(USERNAME).orElseThrow();

            // when
            playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile1);
            playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile2);

            em.flush();
            em.clear();

            List<PlayerCustomPhoto> all = playerCustomPhotoRepository.findAll();

            // then
            assertThat(all).hasSize(2);
            PlayerCustomPhoto photo1 = all.get(0);
            PlayerCustomPhoto photo2 = all.get(1);
            OneIsActiveAndAnotherIsInactive(photo1, photo2);
        }
    }

    @Nested
    class GetPlayerCustomPhotos {

        @DisplayName("선수들의 커스텀 이미지 맵을 가져온다")
        @Test
        void getPlayerCustomPhotos() {
            // given
            MultipartFile multipartFile1 = CustomPhotoMultipartGenerator.generate();
            MultipartFile multipartFile2 = CustomPhotoMultipartGenerator.generate();
            User user = userRepository.findByUsername(USERNAME).orElseThrow();
            playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile1);
            playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER2_ID, multipartFile2);
            final String keyhash = preferenceKey.getKeyhash();

            // when
            Set<Long> playerIds = Set.of(PLAYER1_ID, PLAYER2_ID);
            Map<Long, PlayerCustomPhotoDto> photoDtoMap = playerCustomPhotoService.getActiveCustomPhotos(keyhash, playerIds);

            // then
            assertThat(photoDtoMap).hasSize(2);
            assertThat(photoDtoMap.get(PLAYER1_ID)).isNotNull();
            assertThat(photoDtoMap.get(PLAYER2_ID)).isNotNull();
        }

        @DisplayName("비활성화된 커스텀 이미지는 가져오지 않는다")
        @Test
        void getPlayerCustomPhotoOnlyActive() {
            // given
            MultipartFile multipartFile1 = CustomPhotoMultipartGenerator.generate();
            MultipartFile multipartFile2 = CustomPhotoMultipartGenerator.generate();
            User user = userRepository.findByUsername(USERNAME).orElseThrow();
            playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile1);
            // first photo is deactivated when second photo is uploaded for the same player and the same user
            playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile2);
            em.flush();
            em.clear();
            final String keyhash = preferenceKey.getKeyhash();
            Set<Long> playerIds = Set.of(PLAYER1_ID);

            // when
            Map<Long, PlayerCustomPhotoDto> photoDtoMap = playerCustomPhotoService.getActiveCustomPhotos(keyhash, playerIds);

            List<PlayerCustomPhoto> all = playerCustomPhotoRepository.findAll();
            log.info("all: {}", all);
            // then
            assertThat(photoDtoMap).hasSize(1);
            assertThat(photoDtoMap.get(PLAYER1_ID)).isNotNull();
        }

        @DisplayName("커스텀 이미지가 없는 경우 빈 목록을 가져온다")
        @Test
        void noCustomPhotoExists() {
            // given
            final String keyhash = preferenceKey.getKeyhash();
            Set<Long> playerIds = Set.of(PLAYER1_ID);

            // when
            Map<Long, PlayerCustomPhotoDto> photoDtoMap = playerCustomPhotoService.getActiveCustomPhotos(keyhash, playerIds);

            // then
            assertThat(photoDtoMap).isEmpty();
        }
    }

    @Nested
    class GetAllCustomPhotos {

        @DisplayName("선수의 모든 커스텀 이미지를 가져온다")
        @Test
        void getAllCustomPhotos() {
            // given
            MultipartFile multipartFile1 = CustomPhotoMultipartGenerator.generate();
            MultipartFile multipartFile2 = CustomPhotoMultipartGenerator.generate();
            User user = userRepository.findByUsername(USERNAME).orElseThrow();
            playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile1);
            playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile2);

            // when
            List<PlayerCustomPhotoDto> photoDtoList = playerCustomPhotoService.getAllCustomPhotosWithUsername(USERNAME, PLAYER1_ID);

            // then
            assertThat(photoDtoList).hasSize(2);
        }

        @DisplayName("비활성화된 커스텀 이미지도 모두 가져온다")
        @Test
        void getAllCustomPhotosWithInactive() {
            // given
            MultipartFile multipartFile1 = CustomPhotoMultipartGenerator.generate();
            MultipartFile multipartFile2 = CustomPhotoMultipartGenerator.generate();
            User user = userRepository.findByUsername(USERNAME).orElseThrow();
            playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile1);
            // first photo is deactivated when second photo is uploaded for the same player and the same user
            playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile2);

            // when
            List<PlayerCustomPhotoDto> photoDtoList = playerCustomPhotoService.getAllCustomPhotosWithUsername(USERNAME, PLAYER1_ID);

            // then
            assertThat(photoDtoList).hasSize(2);
        }

        @DisplayName("커스텀 이미지가 없는 경우 빈 목록을 가져온다")
        @Test
        void noCustomPhotoExists() {
            // when
            List<PlayerCustomPhotoDto> photoDtoList = playerCustomPhotoService.getAllCustomPhotosWithUsername(USERNAME, PLAYER1_ID);

            // then
            assertThat(photoDtoList).isEmpty();
        }
    }

    @Nested
    class DeactiveCustomPhoto {

        @DisplayName("커스텀 이미지를 비활성화한다")
        @Test
        void deactiveCustomPhoto() {
            // given
            MultipartFile multipartFile = CustomPhotoMultipartGenerator.generate();
            User user = userRepository.findByUsername(USERNAME).orElseThrow();
            PlayerCustomPhotoDto photoDto = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile);

            // when
            playerCustomPhotoService.deactivatePhotoWithUsername(USERNAME, photoDto.getId());
            em.flush();
            em.clear();

            // then
            PlayerCustomPhoto photo = playerCustomPhotoRepository.findById(photoDto.getId()).orElseThrow();

            assertThat(photo.isActive()).isFalse();
        }

        @DisplayName("전부 비활성화 된 경우에도 true 를 반환")
        @Test
        void alreadyAllPhotosDeactivated() {
            // given
            MultipartFile multipartFile1 = CustomPhotoMultipartGenerator.generate();
            MultipartFile multipartFile2 = CustomPhotoMultipartGenerator.generate();
            User user = userRepository.findByUsername(USERNAME).orElseThrow();
            PlayerCustomPhotoDto photoDto1 = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile1);
            PlayerCustomPhotoDto photoDto2 = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile2);
            em.flush();
            em.clear();

            Long photoId1 = photoDto1.getId();
            Long photoId2 = photoDto2.getId();
            playerCustomPhotoRepository.findById(photoId1).ifPresent(photo -> photo.setActive(false));
            playerCustomPhotoRepository.findById(photoId2).ifPresent(photo -> photo.setActive(false));
            em.flush();
            em.clear();

            // when
            boolean result1 = playerCustomPhotoService.deactivatePhotoWithUsername(USERNAME, photoId1);
            boolean result2 = playerCustomPhotoService.deactivatePhotoWithUsername(USERNAME, photoId2);

            // then
            assertThat(result1).isTrue();
            assertThat(result2).isTrue();
        }
    }

    @Nested
    class ActivatePhoto {

        @DisplayName("이미 활성화된 이미지를 활성화 시켜도 정상적으로 dto 반환")
        @Test
        void activatePhoto() {
            // given
            MultipartFile multipartFile = CustomPhotoMultipartGenerator.generate();
            User user = userRepository.findByUsername(USERNAME).orElseThrow();
            PlayerCustomPhotoDto photoDto = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile);
            long photoId = photoDto.getId();

            // when
            PlayerCustomPhotoDto dto = playerCustomPhotoService.activatePhotoWithUsername(USERNAME, photoId);

            // then
            assertThat(dto).isNotNull();
            assertThat(dto.getId()).isEqualTo(photoId);
            assertThat(dto.getIsActive()).isTrue();

            PlayerCustomPhoto entity = playerCustomPhotoRepository.findById(photoId).orElseThrow();
            assertThat(entity.isActive()).isTrue();
        }

        @DisplayName("활성화 된 이미지가 없는 경우 정상적으로 활성화")
        @Test
        void alreadyAllPhotosActivated() {
            // given
            MultipartFile multipartFile1 = CustomPhotoMultipartGenerator.generate();
            MultipartFile multipartFile2 = CustomPhotoMultipartGenerator.generate();
            User user = userRepository.findByUsername(USERNAME).orElseThrow();
            PlayerCustomPhotoDto photoDto1 = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile1);
            PlayerCustomPhotoDto photoDto2 = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile2);
            em.flush();
            em.clear();

            Long id1 = photoDto1.getId();
            Long id2 = photoDto2.getId();
            playerCustomPhotoRepository.findById(id1).ifPresent(photo -> photo.setActive(false));
            playerCustomPhotoRepository.findById(id2).ifPresent(photo -> photo.setActive(false));
            em.flush();
            em.clear();

            // when
            PlayerCustomPhotoDto dto = playerCustomPhotoService.activatePhotoWithUsername(USERNAME, id1);

            // then
            assertThat(dto).isNotNull();
            assertThat(dto.getId()).isEqualTo(id1);
            assertThat(dto.getIsActive()).isTrue();

            PlayerCustomPhoto entity = playerCustomPhotoRepository.findById(id1).orElseThrow();
            assertThat(entity.isActive()).isTrue();
        }
    }

    @Nested
    class DeletePhoto {

        @DisplayName("커스텀 이미지를 삭제한다")
        @Test
        void deletePhoto() {
            // given
            MultipartFile multipartFile = CustomPhotoMultipartGenerator.generate();
            User user = userRepository.findByUsername(USERNAME).orElseThrow();
            PlayerCustomPhotoDto photoDto = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile);
            final long photoId = photoDto.getId();

            // when
            playerCustomPhotoService.deletePhotoWithUsername(USERNAME, photoId);

            // then
            assertThat(playerCustomPhotoRepository.findById(photoDto.getId())).isEmpty();
        }

        @DisplayName("삭제할 이미지가 없어도 true 를 반환")
        @Test
        void deleteNonExistingPhoto() {
            // given
            final long NOT_EXISTING_PHOTO_ID = 999999L;

            // when
            boolean result = playerCustomPhotoService.deletePhotoWithUsername(USERNAME, NOT_EXISTING_PHOTO_ID);
            em.flush();
            em.clear();

            // then
            assertThat(result).isTrue();
        }
    }

    @Nested
    class DeactivatePhotoWithUsernameAndPlayerId {

        @DisplayName("커스텀 이미지가 없는 경우에도 true 를 반환")
        @Test
        void noCustomPhotoReturnTrue() {
            // given
            List<PlayerCustomPhotoDto> photos = playerCustomPhotoService.getAllCustomPhotosWithUsername(USERNAME, PLAYER1_ID);
            log.info("photos: {}", photos);

            // when
            boolean result = playerCustomPhotoService.deactivatePhotoWithUsernameAndPlayerId(USERNAME, PLAYER1_ID);
            log.info("result: {}", result);

            // then
            assertThat(photos).isEmpty();
            assertThat(result).isTrue();
        }

        @DisplayName("하나의 커스텀 이미지 활성화 상태에서 비활성화")
        @Test
        void oneActivePhoto() {
            // given
            MultipartFile multipartFile = CustomPhotoMultipartGenerator.generate();
            User user = userRepository.findByUsername(USERNAME).orElseThrow();
            PlayerCustomPhotoDto photoDto = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile);
            em.flush();
            em.clear();

            // when
            boolean result = playerCustomPhotoService.deactivatePhotoWithUsernameAndPlayerId(USERNAME, PLAYER1_ID);

            // then
            assertThat(result).isTrue();
            List<PlayerCustomPhotoDto> photos = playerCustomPhotoService.getAllCustomPhotosWithUsername(USERNAME, PLAYER1_ID);
            log.info("photos: {}", photos);
            assertThat(photos).hasSize(1);
            assertThat(photos.get(0).getIsActive()).isFalse();
        }

        @DisplayName("여러개의 커스텀 이미지 중 하나만 활성화 상태에서 비활성화")
        @Test
        void multiplePhotosOneActive() {
            // given
            MultipartFile multipartFile1 = CustomPhotoMultipartGenerator.generate();
            MultipartFile multipartFile2 = CustomPhotoMultipartGenerator.generate();
            User user = userRepository.findByUsername(USERNAME).orElseThrow();
            PlayerCustomPhotoDto photoDto1 = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile1);
            PlayerCustomPhotoDto photoDto2 = playerCustomPhotoService.registerAndUploadCustomPhoto(user.getId(), PLAYER1_ID, multipartFile2);
            em.flush();
            em.clear();

            // when
            boolean result = playerCustomPhotoService.deactivatePhotoWithUsernameAndPlayerId(USERNAME, PLAYER1_ID);

            // then
            assertThat(result).isTrue();
            List<PlayerCustomPhotoDto> photos = playerCustomPhotoService.getAllCustomPhotosWithUsername(USERNAME, PLAYER1_ID);
            log.info("photos: {}", photos);
            assertThat(photos).hasSize(2);
            assertThat(photos.get(0).getIsActive()).isFalse();
            assertThat(photos.get(1).getIsActive()).isFalse();
        }

    }

    private static void OneIsActiveAndAnotherIsInactive(PlayerCustomPhoto photo1, PlayerCustomPhoto photo2) {
        boolean active1 = photo1.isActive();
        boolean active2 = photo2.isActive();
        boolean xor = active1 ^ active2;
        assertThat(xor).isTrue();
    }

}