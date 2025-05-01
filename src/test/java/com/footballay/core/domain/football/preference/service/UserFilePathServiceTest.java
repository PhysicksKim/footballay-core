package com.footballay.core.domain.football.preference.service;

import com.footballay.core.domain.football.preference.persistence.UserFilePath;
import com.footballay.core.domain.football.preference.persistence.UserPathCategory;
import com.footballay.core.domain.football.preference.repository.UserFilePathRepository;
import com.footballay.core.domain.user.entity.User;
import com.footballay.core.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("mockapi")
@Transactional
public class UserFilePathServiceTest {

    @Autowired
    private UserFilePathService userFilePathService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserFilePathRepository userFilePathRepository;

    @Test
    @DisplayName("기존 UserFilePath가 있으면 해당 경로를 반환한다")
    public void testGetPlayerCustomPhotoPath_ExistingPath() {
        // Given: 사용자 생성 및 저장
        User user = User.builder()
                .username("user1")
                .password("password1")
                .build();
        user = userRepository.save(user);

        // Given: 기존 UserFilePath 생성 및 저장
        UserFilePath existingPath = UserFilePath.builder()
                .user(user)
                .domain("https://s3.amazonaws.com/example-domain")
                .prefixPath("chuncity/football/players/")
                .suffixPath("/photo")
                .userPathHash("existingHash1234567")
                .userPathCategory(UserPathCategory.PLAYER_CUSTOM_PHOTO)
                .build();
        existingPath = userFilePathRepository.save(existingPath);

        // When: getPlayerCustomPhotoPath 호출
        UserFilePath retrievedPath = userFilePathService.getPlayerCustomPhotoPath(user);

        // Then: 기존 경로가 반환되는지 확인
        assertThat(retrievedPath).isNotNull();
        assertThat(retrievedPath.getId()).isEqualTo(existingPath.getId());
        assertThat(retrievedPath.getUser()).isEqualTo(user);
        assertThat(retrievedPath.getDomain()).isEqualTo("https://s3.amazonaws.com/example-domain");
        assertThat(retrievedPath.getPrefixPath()).isEqualTo("chuncity/football/players");
        assertThat(retrievedPath.getSuffixPath()).isEqualTo("photo");
        assertThat(retrievedPath.getUserPathHash()).isEqualTo("existingHash1234567");
        assertThat(retrievedPath.getUserPathCategory()).isEqualTo(UserPathCategory.PLAYER_CUSTOM_PHOTO);
    }

    @Test
    @DisplayName("기존 UserFilePath가 없으면 새로운 경로를 생성하여 반환한다")
    public void testGetPlayerCustomPhotoPath_NewPath() {
        // Given: 사용자 생성 및 저장
        User user = User.builder()
                .username("user1")
                .password("password1")
                .build();
        user = userRepository.save(user);

        // Ensure no existing UserFilePath
        Optional<UserFilePath> existingPath = userFilePathRepository.findByUserIdAndUserPathCategory(
                user.getId(),
                UserPathCategory.PLAYER_CUSTOM_PHOTO
        );
        assertThat(existingPath).isNotPresent();

        // When: getPlayerCustomPhotoPath 호출
        UserFilePath newPath = userFilePathService.getPlayerCustomPhotoPath(user);

        // Then: 새로운 UserFilePath가 생성되고 반환되는지 확인
        assertThat(newPath).isNotNull();
        assertThat(newPath.getId()).isNotNull();
        assertThat(newPath.getUser()).isEqualTo(user);
        assertThat(newPath.getUserPathHash()).isNotNull();
        assertThat(newPath.getUserPathHash()).hasSize(16);
        assertThat(newPath.getUserPathCategory()).isEqualTo(UserPathCategory.PLAYER_CUSTOM_PHOTO);

        // Verify that the new path exists in repository
        Optional<UserFilePath> retrieved = userFilePathRepository.findByUserIdAndUserPathCategory(
                user.getId(),
                UserPathCategory.PLAYER_CUSTOM_PHOTO
        );
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getUserPathHash()).isEqualTo(newPath.getUserPathHash());
    }

    @Test
    @DisplayName("생성된 userPathHash가 유니크한지 확인")
    public void testUserPathHashUniqueness() {
        // Given: 두 사용자 생성 및 저장
        User user1 = User.builder()
                .username("user1")
                .password("password1")
                .build();
        user1 = userRepository.save(user1);

        User user2 = User.builder()
                .username("user2")
                .password("password2")
                .build();
        user2 = userRepository.save(user2);

        // Given: 첫 번째 사용자에 대한 UserFilePath 생성
        UserFilePath userFilePath1 = userFilePathService.getPlayerCustomPhotoPath(user1);
        String hash1 = userFilePath1.getUserPathHash();

        // When: 두 번째 사용자에 대한 UserFilePath 생성
        UserFilePath userFilePath2 = userFilePathService.getPlayerCustomPhotoPath(user2);
        String hash2 = userFilePath2.getUserPathHash();

        // Then: 두 해시가 다름을 확인
        assertThat(hash1).isNotEqualTo(hash2);

        // And: 두 해시가 모두 유니크한지 확인
        assertThat(userFilePathRepository.existsByUserPathHash(hash1)).isTrue();
        assertThat(userFilePathRepository.existsByUserPathHash(hash2)).isTrue();
    }
}