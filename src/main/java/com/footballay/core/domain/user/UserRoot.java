package com.footballay.core.domain.user;

import com.footballay.core.domain.football.preference.persistence.PreferenceKey;
import com.footballay.core.domain.user.entity.Authority;
import com.footballay.core.domain.user.entity.Role;
import com.footballay.core.domain.user.entity.User;
import com.footballay.core.domain.user.service.UserService;
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
        PreferenceKey key = user.getPreferenceKey();
        String preferenceKey = key == null ? "" : key.getKeyhash();
        return new UserInfoDto(nickname, roles, profileImage, preferenceKey);
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
