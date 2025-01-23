package com.gyechunsik.scoreboard.domain.user;

public record UserInfoDto(
        String nickname,
        String[] roles,
        String profileImage,
        String preferenceKey
) {
}
