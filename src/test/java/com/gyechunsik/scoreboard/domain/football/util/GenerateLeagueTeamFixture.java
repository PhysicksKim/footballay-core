package com.gyechunsik.scoreboard.domain.football.util;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.League;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class GenerateLeagueTeamFixture {

    // _StandingResponseData
    public static final Long leagueId = 39L;
    public static final String name = "Premier _StandingResponseData";
    public static final String koreanName = "프리미어리그";
    public static final String logo = "https://media.api-sports.io/football/leagues/39.png";
    public static final Integer currentSeason = 2023;

    public static final Long leagueId2 = 4L;
    public static final String name2 = "Euro Championship";
    public static final String koreanName2 = "유로 챔피언십";
    public static final String logo2 = "https://media.api-sports.io/football/leagues/4.png";
    public static final Integer currentSeason2 = 2024;

    // _Team
    public static final Long homeId = 33L;
    public static final String homeName = "Manchester United";
    public static final String homeKoreanName = "맨체스터 유나이티드";
    public static final String homeLogo = "https://media.api-sports.io/football/teams/33.png";
    public static final Long awayId = 50L;
    public static final String awayName = "Manchester City";
    public static final String awayKoreanName = "맨체스터 시티";
    public static final String awayLogo = "https://media.api-sports.io/football/teams/50.png";

    public static final Long homeId2 = 49L;
    public static final String homeName2 = "Chelsea";
    public static final String homeKoreanName2 = "첼시";
    public static final String homeLogo2 = "https://media.api-sports.io/football/teams/49.png";
    public static final Long awayId2 = 42L;
    public static final String awayName2 = "Arsenal";
    public static final String awayKoreanName2 = "아스날";
    public static final String awayLogo2 = "https://media.api-sports.io/football/teams/42.png";

    public static final Long homeId3 = 1L;
    public static final String homeName3 = "Belgium";
    public static final String homeKoreanName3 = "벨기에";
    public static final String homeLogo3 = "https://media.api-sports.io/football/teams/1.png";
    public static final Long awayId3 = 2L;
    public static final String awayName3 = "France";
    public static final String awayKoreanName3 = "프랑스";
    public static final String awayLogo3 = "https://media.api-sports.io/football/teams/2.png";

    // _Fixture
    public static final Long fixtureId = 123456L;
    public static final String referee = "Anthony Taylor";
    public static final String timezone = "UTC";
    public static final LocalDateTime date = LocalDateTime.parse("2024-06-14T19:00:00+00:00", DateTimeFormatter.ISO_DATE_TIME);
    public static final Long timestamp = ZonedDateTime.of(date, ZoneId.of(timezone)).toInstant().toEpochMilli();
    // public static final _Fixture._Status status = _Fixture._Status.builder()
    //         .longStatus("Not Started")
    //         .shortStatus("NS")
    //         .elapsed(null)
    //         .build();
    public static final Long fixtureId2 = 789012L;
    public static final Long fixtureId3 = 345678L;

    @AllArgsConstructor
    public static class LeagueTeamFixture {
        public League league;
        public Team home;
        public Team away;
        public Fixture fixture;
    }

    private static LeagueTeamFixture generateFirstSet() {
        League league = League.builder()
                .leagueId(leagueId)
                .name(name)
                .koreanName(koreanName)
                .logo(logo)
                .currentSeason(currentSeason)
                .build();
        Team home = Team.builder()
                .id(homeId)
                .name(homeName)
                .koreanName(homeKoreanName)
                .logo(homeLogo)
                .build();
        Team away = Team.builder()
                .id(awayId)
                .name(awayName)
                .koreanName(awayKoreanName)
                .logo(awayLogo)
                .build();
        Fixture fixture = Fixture.builder()
                .fixtureId(fixtureId)
                .referee(referee)
                .timezone(timezone)
                .date(date)
                .timestamp(timestamp)
                // .status(status)
                .homeTeam(home)
                .awayTeam(away)
                .league(league)
                .build();
        return new LeagueTeamFixture(league, home, away, fixture);
    }

    private static LeagueTeamFixture generateSecondSet() {
        League league = League.builder()
                .leagueId(leagueId)
                .name(name)
                .koreanName(koreanName)
                .logo(logo)
                .currentSeason(currentSeason)
                .build();
        Team home = Team.builder()
                .id(homeId2)
                .name(homeName2)
                .koreanName(homeKoreanName2)
                .logo(homeLogo2)
                .build();
        Team away = Team.builder()
                .id(awayId2)
                .name(awayName2)
                .koreanName(awayKoreanName2)
                .logo(awayLogo2)
                .build();
        Fixture fixture = Fixture.builder()
                .fixtureId(fixtureId2)
                .referee(referee)
                .timezone(timezone)
                .date(date)
                .timestamp(timestamp)
                // .status(status)
                .homeTeam(home)
                .awayTeam(away)
                .league(league)
                .build();
        return new LeagueTeamFixture(league, home, away, fixture);
    }

    public static LeagueTeamFixture generateThirdSet() {
        League league = League.builder()
                .leagueId(leagueId2)
                .name(name2)
                .koreanName(koreanName2)
                .logo(logo2)
                .currentSeason(currentSeason2)
                .build();
        Team home = Team.builder()
                .id(homeId3)
                .name(homeName3)
                .koreanName(homeKoreanName3)
                .logo(homeLogo3)
                .build();
        Team away = Team.builder()
                .id(awayId3)
                .name(awayName3)
                .koreanName(awayKoreanName3)
                .logo(awayLogo3)
                .build();
        Fixture fixture = Fixture.builder()
                .fixtureId(fixtureId3)
                .referee(referee)
                .timezone(timezone)
                .date(date)
                .timestamp(timestamp)
                // .status(status)
                .homeTeam(home)
                .awayTeam(away)
                .league(league)
                .build();
        return new LeagueTeamFixture(league, home, away, fixture);
    }

    public static LeagueTeamFixture generate() {
        return generateFirstSet();
    }

    public static List<LeagueTeamFixture> generateTwoSameLeague() {
        return List.of(generateFirstSet(), generateSecondSet());
    }

    public static List<LeagueTeamFixture> generateTwoOtherLeagues() {
        return List.of(generateFirstSet(), generateThirdSet());
    }

}
