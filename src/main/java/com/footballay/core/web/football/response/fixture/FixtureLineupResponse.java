package com.footballay.core.web.football.response.fixture;

import java.util.List;

/**
 * 경기 라인업 정보 응답 DTO.
 * 라인업이 아직 저장되지 않았다면 빈 라인업이 제공됩니다.
 * @param fixtureId
 * @param lineup
 */
public record FixtureLineupResponse(
        long fixtureId,
        _Lineup lineup
) {

    public record _Lineup (
            _StartLineup home,
            _StartLineup away
    ) {
    }

    public record _StartLineup(
            long teamId,
            String teamName,
            String teamKoreanName,
            String formation,
            List<_LineupPlayer> players,
            List<_LineupPlayer> substitutes
    ) {
    }

    public record _LineupPlayer(
            long id,
            String koreanName,
            String name,
            Integer number,
            String photo,
            String position,
            String grid,
            boolean substitute,
            String tempId
    ) {
    }
}
