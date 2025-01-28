package com.gyechunsik.scoreboard.domain.user;

import com.gyechunsik.scoreboard.domain.user.entity.Authority;
import com.gyechunsik.scoreboard.domain.user.entity.Role;
import com.gyechunsik.scoreboard.domain.user.entity.User;
import com.gyechunsik.scoreboard.domain.user.repository.AuthorityRepository;
import com.gyechunsik.scoreboard.domain.user.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class UserRootTest {

    @Autowired
    private UserRoot userRoot;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthorityRepository authorityRepository;

    private static final String TEST_USERNAME = "usertester";
    private static final String TEST_PASSWORD = "test1";
    private static final String TEST_NICKNAME = "유저테스터";
    private static final String TEST_USERNAME2 = "admintester";
    private static final String TEST_PASSWORD2 = "test2";
    private static final String TEST_NICKNAME2 = "어드민테스터";
    private static final String TEST_USERNAME3 = "multiroletester";
    private static final String TEST_PASSWORD3 = "test3";
    private static final String TEST_NICKNAME3 = "멀티롤테스터";

    @BeforeEach
    void setUp() {
        User user = saveUser(TEST_USERNAME, TEST_NICKNAME, TEST_PASSWORD);
        User admin = saveUser(TEST_USERNAME2, TEST_NICKNAME2, TEST_PASSWORD2);
        User multiRole = saveUser(TEST_USERNAME3, TEST_NICKNAME3, TEST_PASSWORD3);

        saveAuthority(user, Role.ROLE_USER);
        saveAuthority(admin, Role.ROLE_ADMIN);
        saveAuthority(multiRole, Role.ROLE_USER);
        saveAuthority(multiRole, Role.ROLE_ADMIN);
    }

    @AfterEach
    void tearDown() {
        authorityRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Nested
    @DisplayName("savePlayerCustomPhoto 메서드")
    class SavePlayerCustomPhotoTest {

        enum ResponseRole {
            admin,user
        }

        @DisplayName("유저 정보를 가져옵니다")
        @Test
        void successRetrieveUserInfo() {
            // when
            UserInfoDto userInfo = userRoot.getUserInfo(TEST_USERNAME);
            logUserInfoDto(userInfo);

            // then
            assertThat(userInfo.nickname()).isEqualTo(TEST_NICKNAME);
            assertThat(userInfo.roles()).contains("user");
            assertThat(userInfo.roles()).doesNotContain("admin");
        }

        @DisplayName("관리자 정보를 가져옵니다")
        @Test
        void successRetrieveAdminInfo() {
            // when
            UserInfoDto userInfo = userRoot.getUserInfo(TEST_USERNAME2);
            logUserInfoDto(userInfo);

            // then
            assertThat(userInfo.nickname()).isEqualTo(TEST_NICKNAME2);
            assertThat(userInfo.roles()).contains("admin");
            assertThat(userInfo.roles()).doesNotContain("user");
        }

        @DisplayName("멀티롤 유저 정보를 가져옵니다")
        @Test
        void successRetrieveMultiRoleInfo() {
            // when
            UserInfoDto userInfo = userRoot.getUserInfo(TEST_USERNAME3);
            logUserInfoDto(userInfo);

            // then
            assertThat(userInfo.nickname()).isEqualTo(TEST_NICKNAME3);
            assertThat(userInfo.roles()).contains("user");
            assertThat(userInfo.roles()).contains("admin");
        }
    }

    private void saveAuthority(User user, Role role) {
        Authority authority = new Authority();
        authority.setAuthority(role);
        authority.setUser(user);
        authorityRepository.save(authority);
    }

    private User saveUser(String username, String nickname, String password) {
        User user = User.builder()
                .username(username)
                .nickname(nickname)
                .password(password)
                .build();
        userRepository.save(user);
        return user;
    }

    private static void logUserInfoDto(UserInfoDto userInfo) {
        log.info("nickname={}", userInfo.nickname());
        log.info("roles={}", Arrays.toString(userInfo.roles()));
    }

}