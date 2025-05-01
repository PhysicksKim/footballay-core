package com.footballay.core.domain.football.dto;

public record FixtureEventWithPlayerDto(
        int sequence,
        int elapsed,
        int extraTime,
        EventTeamDto team,
        EventPlayerDto player,
        EventPlayerDto assist,
        String type,
        String detail,
        String comments
) {
    public record EventTeamDto(
            long teamId,
            String name,
            String koreanName
    ) {
    }

    public record EventPlayerDto(
            Long playerId,
            String name,
            String koreanName,
            Integer number,
            String tempId
    ) {
    }

}
