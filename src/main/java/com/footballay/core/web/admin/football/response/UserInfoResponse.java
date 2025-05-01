package com.footballay.core.web.admin.football.response;

public record UserInfoResponse(
        String nickname,
        String[] roles,
        String profileImage,
        String preferenceKey
) {
}
