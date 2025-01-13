package com.gyechunsik.scoreboard.domain.football.preference.repository;

import com.gyechunsik.scoreboard.domain.football.preference.persistence.PreferenceKey;
import com.gyechunsik.scoreboard.domain.user.entity.User;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@DataJpaTest
@Transactional
class PreferenceKeyRepositoryTest {

    @Autowired
    private PreferenceKeyRepository preferenceKeyRepository;

    @Autowired
    private EntityManager testEntityManager;

    private User user1;
    private User user2;
    private PreferenceKey preferenceKey1;
    private PreferenceKey preferenceKey2;

    /**
     * 테스트용 엔티티들
     * <h4>User</h4>
     * <pre>
     * | id | username | password | enabled |
     * |----|----------|----------|---------|
     * | 1  | user1    | password1| true    |
     * | 2  | user2    | password2| true    |
     * </pre>
     * <h4>PreferenceKey</h4>
     * <pre>
     * | id | user_id | keyhash |
     * |----|---------|---------|
     * | 1  | 1       | prefKey1|
     * | 2  | 2       | prefKey2|
     * </pre>
     */
    @BeforeEach
    void setUp() {
        // 테스트용 User 생성
        user1 = User.builder()
                .username("user1")
                .password("password1")
                .enabled(true)
                .build();

        user2 = User.builder()
                .username("user2")
                .password("password2")
                .enabled(true)
                .build();

        // User 엔티티 영속화
        testEntityManager.persist(user1);
        testEntityManager.persist(user2);
        testEntityManager.flush();
        testEntityManager.clear();

        // 테스트용 PreferenceKey 생성 (각 사용자당 하나씩만 생성)
        preferenceKey1 = PreferenceKey.builder()
                .keyhash("prefKey1")
                .user(user1)
                .build();

        preferenceKey2 = PreferenceKey.builder()
                .keyhash("prefKey2")
                .user(user2)
                .build();

        // PreferenceKey 엔티티 영속화
        testEntityManager.persist(preferenceKey1);
        testEntityManager.persist(preferenceKey2);
        testEntityManager.flush();
        testEntityManager.clear();
    }

    @Nested
    @DisplayName("existsByKeyhash 메서드 테스트")
    class ExistsByKeyhashTest {

        @Test
        @DisplayName("존재하는 keyhash에 대해 true 반환")
        void existsByKeyhash_True() {
            // given
            String existingKeyhash = "prefKey1";

            // when
            boolean exists = preferenceKeyRepository.existsByKeyhash(existingKeyhash);

            // then
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("존재하지 않는 keyhash에 대해 false 반환")
        void existsByKeyhash_False() {
            // given
            String nonExistingKeyhash = "nonExistingKeyhash";

            // when
            boolean exists = preferenceKeyRepository.existsByKeyhash(nonExistingKeyhash);

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("빈 문자열 keyhash에 대해 false 반환")
        void existsByKeyhash_Empty() {
            // given
            String emptyKeyhash = "";

            // when
            boolean exists = preferenceKeyRepository.existsByKeyhash(emptyKeyhash);

            // then
            assertThat(exists).isFalse();
        }

        @Test
        @DisplayName("null keyhash에 대해 false 반환")
        void existsByKeyhash_Null() {
            // given
            String nullKeyhash = null;

            // when
            boolean exists = preferenceKeyRepository.existsByKeyhash(nullKeyhash);

            // then
            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("findByUserId 메서드 테스트")
    class FindByUserIdTest {

        @Test
        @DisplayName("존재하는 userId에 대해 PreferenceKey 반환")
        void findByUserIdAndKeyhash_Found() {
            // given
            Long userId = user1.getId();
            String keyhash = "prefKey1";

            // when
            Optional<PreferenceKey> result = preferenceKeyRepository.findByUserId(userId);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getKeyhash()).isEqualTo(keyhash);
            assertThat(result.get().getUser().getId()).isEqualTo(userId);
        }

        @Test
        @DisplayName("존재하지 않는 userId에 대해 Optional.empty 반환")
        void findByUserIdAndKeyhash_NotFound() {
            // given
            Long nonExistingUserId = 999L;

            // when
            Optional<PreferenceKey> result = preferenceKeyRepository.findByUserId(nonExistingUserId);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null userId에 대해 Optional.empty 반환")
        void findByUserIdAndKeyhash_NullKeyhash() {
            // given
            Long userId = null;

            // when
            Optional<PreferenceKey> result = preferenceKeyRepository.findByUserId(userId);

            // then
            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByKeyhash 메서드 테스트")
    class FindByKeyhashTest {

        @Test
        @DisplayName("존재하는 keyhash에 대해 PreferenceKey 반환")
        void findByKeyhash_Found() {
            // given
            String keyhash = "prefKey2";

            // when
            Optional<PreferenceKey> result = preferenceKeyRepository.findByKeyhash(keyhash);

            // then
            assertThat(result).isPresent();
            assertThat(result.get().getKeyhash()).isEqualTo(keyhash);
            assertThat(result.get().getUser().getUsername()).isEqualTo("user2");
        }

        @Test
        @DisplayName("존재하지 않는 keyhash에 대해 Optional.empty 반환")
        void findByKeyhash_NotFound() {
            // given
            String nonExistingKeyhash = "nonExistingPrefKey";

            // when
            Optional<PreferenceKey> result = preferenceKeyRepository.findByKeyhash(nonExistingKeyhash);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("빈 문자열 keyhash에 대해 Optional.empty 반환")
        void findByKeyhash_Empty() {
            // given
            String emptyKeyhash = "";

            // when
            Optional<PreferenceKey> result = preferenceKeyRepository.findByKeyhash(emptyKeyhash);

            // then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("null keyhash에 대해 Optional.empty 반환")
        void findByKeyhash_Null() {
            // given
            String nullKeyhash = null;

            // when
            Optional<PreferenceKey> result = preferenceKeyRepository.findByKeyhash(nullKeyhash);

            // then
            assertThat(result).isEmpty();
        }
    }
}
