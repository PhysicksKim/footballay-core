package com.footballay.core.domain.football.preference.repository;

import com.footballay.core.domain.football.preference.persistence.UserFilePath;
import com.footballay.core.domain.football.preference.persistence.UserPathCategory;
import com.footballay.core.domain.user.entity.User;
import com.footballay.core.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserFilePathEntityTest {

    @Autowired
    private UserFilePathRepository userFilePathRepository;

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("fullPath URL 을 슬래쉬 누락이나 중복 없이 생성하는 테스트")
    class FullPathCleaning {

        @Test
        @DisplayName("prefix와 suffix Path 가 슬래시 없이 주어지는 경우")
        void testFullPath_AllComponentsPresent_NoSlashes() {
            // Given
            User user = User.builder()
                    .username("user1")
                    .password("password1")
                    .build();
            user = userRepository.save(user);

            UserFilePath userFilePath = UserFilePath.builder()
                    .user(user)
                    .domain("https://example.com")
                    .prefixPath("prefixPath")
                    .suffixPath("suffixPath")
                    .userPathHash("hash123456789012")
                    .userPathCategory(UserPathCategory.PLAYER_CUSTOM_PHOTO)
                    .build();
            userFilePath = userFilePathRepository.save(userFilePath);

            // When
            String fullPath = userFilePath.getFullPath();

            // Then
            assertThat(fullPath).isEqualTo("https://example.com/prefixPath/hash123456789012/suffixPath/");
        }

        @Test
        @DisplayName("Domain 뒤에 슬래시가 주어지는 경우")
        void testFullPath_DomainWithTrailingSlash() {
            // Given
            User user = User.builder()
                    .username("user1")
                    .password("password1")
                    .build();
            user = userRepository.save(user);

            UserFilePath userFilePath = UserFilePath.builder()
                    .user(user)
                    .domain("https://example.com/")
                    .prefixPath("prefixPath")
                    .suffixPath("suffixPath")
                    .userPathHash("hashABCDEFGHIJKLMN")
                    .userPathCategory(UserPathCategory.PLAYER_CUSTOM_PHOTO)
                    .build();
            userFilePath = userFilePathRepository.save(userFilePath);

            // When
            String fullPath = userFilePath.getFullPath();

            // Then
            assertThat(fullPath).isEqualTo("https://example.com/prefixPath/hashABCDEFGHIJKLMN/suffixPath/");
        }

        @Test
        @DisplayName("prefix path 앞뒤에 슬래시가 주어지는 경우")
        void testFullPath_PrefixPathWithSlashes() {
            // Given
            User user = User.builder()
                    .username("user1")
                    .password("password1")
                    .build();
            user = userRepository.save(user);

            UserFilePath userFilePath = UserFilePath.builder()
                    .user(user)
                    .domain("https://example.com")
                    .prefixPath("/prefixPath/")
                    .suffixPath("suffixPath")
                    .userPathHash("hashNOPQRSTUVWXYZ")
                    .userPathCategory(UserPathCategory.PLAYER_CUSTOM_PHOTO)
                    .build();
            userFilePath = userFilePathRepository.save(userFilePath);

            // When
            String fullPath = userFilePath.getFullPath();

            // Then
            assertThat(fullPath).isEqualTo("https://example.com/prefixPath/hashNOPQRSTUVWXYZ/suffixPath/");
        }

        @Test
        @DisplayName("suffix path 앞뒤에 슬래시가 주어지는 경우")
        void testFullPath_SuffixPathWithSlashes() {
            // Given
            User user = User.builder()
                    .username("user1")
                    .password("password1")
                    .build();
            user = userRepository.save(user);

            UserFilePath userFilePath = UserFilePath.builder()
                    .user(user)
                    .domain("https://example.com")
                    .prefixPath("prefixPath")
                    .suffixPath("/suffixPath/")
                    .userPathHash("hash1234ABCDEFGH")
                    .userPathCategory(UserPathCategory.PLAYER_CUSTOM_PHOTO)
                    .build();
            userFilePath = userFilePathRepository.save(userFilePath);

            // When
            String fullPath = userFilePath.getFullPath();

            // Then
            assertThat(fullPath).isEqualTo("https://example.com/prefixPath/hash1234ABCDEFGH/suffixPath/");
        }

        @Test
        @DisplayName("prefix path 가 비어있는 경우")
        void testFullPath_EmptyPrefixPath() {
            // Given
            User user = User.builder()
                    .username("user1")
                    .password("password1")
                    .build();
            user = userRepository.save(user);

            UserFilePath userFilePath = UserFilePath.builder()
                    .user(user)
                    .domain("https://example.com")
                    .prefixPath("")
                    .suffixPath("suffixPath")
                    .userPathHash("hash5678IJKLMNOP")
                    .userPathCategory(UserPathCategory.PLAYER_CUSTOM_PHOTO)
                    .build();
            userFilePath = userFilePathRepository.save(userFilePath);

            // When
            String fullPath = userFilePath.getFullPath();

            // Then
            assertThat(fullPath).isEqualTo("https://example.com/hash5678IJKLMNOP/suffixPath/");
        }

        @Test
        @DisplayName("suffix path 가 비어있는 경우")
        void testFullPath_EmptySuffixPath() {
            // Given
            User user = User.builder()
                    .username("user1")
                    .password("password1")
                    .build();
            user = userRepository.save(user);

            UserFilePath userFilePath = UserFilePath.builder()
                    .user(user)
                    .domain("https://example.com")
                    .prefixPath("prefixPath")
                    .suffixPath("")
                    .userPathHash("hash9012QRSTUVWX")
                    .userPathCategory(UserPathCategory.PLAYER_CUSTOM_PHOTO)
                    .build();
            userFilePath = userFilePathRepository.save(userFilePath);

            // When
            String fullPath = userFilePath.getFullPath();

            // Then
            assertThat(fullPath).isEqualTo("https://example.com/prefixPath/hash9012QRSTUVWX/");
        }

        @Test
        @DisplayName("prefix path 와 suffix path 가 모두 비어있는 경우")
        void testFullPath_EmptyPrefixAndSuffixPath() {
            // Given
            User user = User.builder()
                    .username("user1")
                    .password("password1")
                    .build();
            user = userRepository.save(user);

            UserFilePath userFilePath = UserFilePath.builder()
                    .user(user)
                    .domain("https://example.com")
                    .prefixPath("")
                    .suffixPath("")
                    .userPathHash("hash3456YZABCDEF")
                    .userPathCategory(UserPathCategory.PLAYER_CUSTOM_PHOTO)
                    .build();
            userFilePath = userFilePathRepository.save(userFilePath);

            // When
            String fullPath = userFilePath.getFullPath();

            // Then
            assertThat(fullPath).isEqualTo("https://example.com/hash3456YZABCDEF/");
        }

        @Test
        @DisplayName("prefix path 와 suffix path 가 모두 앞 뒤에 슬래시가 주어지는 경우")
        void testPathCleaningBeforeSave() {
            // Given
            User user = User.builder()
                    .username("user1")
                    .password("password1")
                    .build();
            user = userRepository.save(user);

            UserFilePath userFilePath = UserFilePath.builder()
                    .user(user)
                    .domain("https://example.com/")
                    .prefixPath("/prefixPath/")
                    .suffixPath("/suffixPath/")
                    .userPathHash("hash7890GHIJKLMN")
                    .userPathCategory(UserPathCategory.PLAYER_CUSTOM_PHOTO)
                    .build();
            userFilePath = userFilePathRepository.save(userFilePath);

            // When
            UserFilePath retrievedPath = userFilePathRepository.findById(userFilePath.getId()).orElse(null);

            // Then
            assertThat(retrievedPath).isNotNull();
            assertThat(retrievedPath.getDomain()).isEqualTo("https://example.com");
            assertThat(retrievedPath.getPrefixPath()).isEqualTo("prefixPath");
            assertThat(retrievedPath.getSuffixPath()).isEqualTo("suffixPath");
        }
    }
}