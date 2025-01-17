package com.gyechunsik.scoreboard.domain.user;

import com.gyechunsik.scoreboard.domain.user.entity.Authority;
import com.gyechunsik.scoreboard.domain.user.entity.Role;
import com.gyechunsik.scoreboard.domain.user.entity.User;
import com.gyechunsik.scoreboard.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;

@Slf4j
@RequiredArgsConstructor
@Component
public class UserRoot {

    private final UserService userService;

    @Transactional(readOnly = true)
    public UserInfoDto getUserInfo(String username) {
        User user = userService.findUser(username);
        String nickname = user.getNickname();
        String[] roles = toRoleResponse(user.getAuthorities());
        String profileImage = user.getProfileImage();
        return new UserInfoDto(nickname, roles, profileImage);
    }

    private static String[] toRoleResponse(Collection<Authority> authorities) {
        return authorities.stream()
                .map(authority -> {
                    Role role = authority.getAuthority();
                    return switch (role) {
                        case ROLE_ADMIN -> "admin";
                        case ROLE_STREAMER -> "streamer";
                        case ROLE_USER -> "user";
                        default -> "unknown";
                    };
                })
                .toArray(String[]::new);
    }

}
