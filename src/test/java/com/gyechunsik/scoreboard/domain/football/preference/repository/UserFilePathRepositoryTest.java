package com.gyechunsik.scoreboard.domain.football.preference.repository;

import com.gyechunsik.scoreboard.domain.football.preference.persistence.UserFilePath;
import com.gyechunsik.scoreboard.domain.football.preference.persistence.UserPathCategory;
import com.gyechunsik.scoreboard.domain.user.entity.User;
import com.gyechunsik.scoreboard.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserFilePathRepositoryTest {

    @Autowired
    private UserFilePathRepository userFilePathRepository;

    @Autowired
    private UserRepository userRepository;

    @Nested
    @DisplayName("existsByUserPathHash 메서드 테서트")
    class ExistsByUserPathHash {

        @Test
        @DisplayName("존재하는 userPathHash 에 대해 true 를 반환")
        public void testExistsByUserPathHash_Exists() {
            // Given: 사용자 생성 및 저장
            User user = User.builder()
                    .username("user1")
                    .password("password1")
                    .build();
            user = userRepository.save(user);

            // Given: UserFilePath 생성 및 저장
            UserFilePath userFilePath = UserFilePath.builder()
                    .user(user)
                    .domain("https://s3.amazonaws.com/example-domain")
                    .prefixPath("/chuncity/football/preference")
                    .suffixPath("/photo")
                    .userPathHash("uniqueHash123")
                    .userPathCategory(UserPathCategory.PLAYER_CUSTOM_PHOTO)
                    .build();
            userFilePathRepository.save(userFilePath);

            // When: existsByUserPathHash 호출
            boolean exists = userFilePathRepository.existsByUserPathHash("uniqueHash123");

            // Then: 존재함을 확인
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 userPathHash 에 대해 false 를 반환")
        public void testExistsByUserPathHash_NotExists() {
            // Given: 존재하지 않는 userPathHash
            String nonExistingHash = "nonExistingHash456";

            // When: existsByUserPathHash 호출
            boolean exists = userFilePathRepository.existsByUserPathHash(nonExistingHash);

            // Then: 존재하지 않음을 확인
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("findByUserIdAndUserPathCategory 메서드 테스트")
    class FindByUserIdAndUserPathCategory {

        @Test
        @DisplayName("UserFilePath 저장 및 조회 테스트")
        public void testSaveAndFindByUserIdAndUserPathCategory() {
            // Given: 사용자 생성 및 저장
            User user = User.builder()
                    .username("user1")
                    .password("password1")
                    .build();
            user = userRepository.save(user);

            // Given: UserFilePath 생성 및 저장
            UserFilePath userFilePath = UserFilePath.builder()
                    .user(user)
                    .domain("https://s3.amazonaws.com/example-domain")
                    .prefixPath("/chuncity/football/preference")
                    .suffixPath("/photo")
                    .userPathHash("uniqueHash123")
                    .userPathCategory(UserPathCategory.PLAYER_CUSTOM_PHOTO)
                    .build();
            userFilePath = userFilePathRepository.save(userFilePath);

            // When: 특정 userId와 UserPathCategory로 조회
            Optional<UserFilePath> retrieved = userFilePathRepository.findByUserIdAndUserPathCategory(
                    user.getId(),
                    UserPathCategory.PLAYER_CUSTOM_PHOTO
            );

            // Then: 조회된 UserFilePath가 존재하고, 값이 일치하는지 확인
            assertThat(retrieved).isPresent();
            UserFilePath found = retrieved.get();
            assertThat(found.getId()).isEqualTo(userFilePath.getId());
            assertThat(found.getUser()).isEqualTo(user);
            assertThat(found.getDomain()).isEqualTo("https://s3.amazonaws.com/example-domain");
            assertThat(found.getPrefixPath()).isEqualTo("chuncity/football/preference");
            assertThat(found.getSuffixPath()).isEqualTo("photo");
            assertThat(found.getUserPathHash()).isEqualTo("uniqueHash123");
            assertThat(found.getUserPathCategory()).isEqualTo(UserPathCategory.PLAYER_CUSTOM_PHOTO);
        }

        @Test
        @DisplayName("존재하지 않는 userId와 UserPathCategory로 조회 시 빈 결과 반환 테스트")
        public void testFindByUserIdAndUserPathCategory_NotFound() {
            // Given: 존재하지 않는 userId와 UserPathCategory
            Long nonExistingUserId = 999L;
            UserPathCategory category = UserPathCategory.PLAYER_CUSTOM_PHOTO;

            // When: 조회
            Optional<UserFilePath> retrieved = userFilePathRepository.findByUserIdAndUserPathCategory(
                    nonExistingUserId,
                    category
            );

            // Then: 결과가 비어있음을 확인
            assertThat(retrieved).isNotPresent();
        }

        @Test
        @DisplayName("userPathHash의 유니크 제약조건 테스트")
        public void testUniqueUserPathHashConstraint() {
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

            // Given: 첫 번째 UserFilePath 저장
            UserFilePath userFilePath1 = UserFilePath.builder()
                    .user(user1)
                    .domain("https://s3.amazonaws.com/domain1")
                    .prefixPath("/path1")
                    .suffixPath("/suffix1")
                    .userPathHash("duplicateHash")
                    .userPathCategory(UserPathCategory.PLAYER_CUSTOM_PHOTO)
                    .build();
            userFilePathRepository.save(userFilePath1);

            // When & Then: 두 번째 UserFilePath에 동일한 userPathHash를 저장하려고 시도하면 예외 발생
            UserFilePath userFilePath2 = UserFilePath.builder()
                    .user(user2)
                    .domain("https://s3.amazonaws.com/domain2")
                    .prefixPath("/path2")
                    .suffixPath("/suffix2")
                    .userPathHash("duplicateHash") // 중복된 해시
                    .userPathCategory(UserPathCategory.PLAYER_CUSTOM_PHOTO)
                    .build();

            // 유니크 제약조건 위반 시 DataIntegrityViolationException 발생
            assertThrows(
                    DataIntegrityViolationException.class,
                    () -> userFilePathRepository.saveAndFlush(userFilePath2)
            );
        }
    }

}