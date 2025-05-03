package com.footballay.core.domain.user;

public record UserInfoDto(
        String nickname,
        String[] roles,
        String profileImage,
        String preferenceKey
) {
}
