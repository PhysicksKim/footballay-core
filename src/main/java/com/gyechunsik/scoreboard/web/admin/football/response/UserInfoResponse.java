package com.gyechunsik.scoreboard.web.admin.football.response;

public record UserInfoResponse(
        String nickname,
        String[] roles,
        String profileImage
) {
}
