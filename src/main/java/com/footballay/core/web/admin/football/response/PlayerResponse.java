package com.footballay.core.web.admin.football.response;

public record PlayerResponse(
        long playerId,
        String name,
        String koreanName,
        String photoUrl,
        String position
        ) {
}
