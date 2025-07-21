package com.footballay.core.domain.football.dto;

import com.footballay.core.domain.football.external.fetch.ApiStatus;
import com.footballay.core.domain.football.persistence.Fixture;
import com.footballay.core.domain.football.persistence.League;
import com.footballay.core.domain.football.persistence.Player;
import com.footballay.core.domain.football.persistence.Team;
import com.footballay.core.domain.football.persistence.live.*;
import org.springframework.lang.Nullable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class FootballDomainDtoMapper {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FootballDomainDtoMapper.class);

    /**
     * 통계 정보가 포함된 dto 를 생성합니다.
     * @param fixture
     * @param homeStats
     * @param awayStats
     * @param homePlayerStats PlayerStatistics 가 load 된 Home team MatchPlayer List
     * @param awayPlayerStats PlayerStatistics 가 load 된 Away team MatchPlayer List
     * @return MatchStatisticsDto
     */
    public static MatchStatisticsDto matchStatisticsDTOFromEntity(Fixture fixture, @Nullable TeamStatistics homeStats, @Nullable TeamStatistics awayStats, List<MatchPlayer> homePlayerStats, List<MatchPlayer> awayPlayerStats) {
        assertStatsMatchFixtureTeams(fixture, homeStats, awayStats);
        log.debug("fixtureId={} DTO mapper start", fixture.getFixtureId());
        MatchStatisticsDto.MatchStatsFixture matchStatsFixture = createFixtureDTO(fixture);
        MatchStatisticsDto.MatchStatsLiveStatus matchStatsLiveStatus = createLiveStatusDTO(fixture.getLiveStatus());
        MatchStatisticsDto.MatchStatsTeam homeDTO = createTeamDTO(fixture.getHomeTeam());
        MatchStatisticsDto.MatchStatsTeam awayDTO = createTeamDTO(fixture.getAwayTeam());
        MatchStatisticsDto.MatchStatsTeamStatistics homeStatisticsDTO = createTeamStatisticsDTO(homeStats);
        MatchStatisticsDto.MatchStatsTeamStatistics awayStatisticsDTO = createTeamStatisticsDTO(awayStats);
        List<MatchStatisticsDto.MatchStatsPlayers> homePlayerStatisticsDTO = createListOfMatchPlayerStatisticsDTO(homePlayerStats);
        List<MatchStatisticsDto.MatchStatsPlayers> awayPlayerStatisticsDTO = createListOfMatchPlayerStatisticsDTO(awayPlayerStats);
        MatchStatisticsDto dto = createMatchStatisticsDTO(matchStatsFixture, matchStatsLiveStatus, homeDTO, awayDTO, homeStatisticsDTO, awayStatisticsDTO, homePlayerStatisticsDTO, awayPlayerStatisticsDTO);
        log.debug("MatchStatisticsDto: {}", dto);
        return dto;
    }

    public static List<LeagueDto> leagueDtosFromEntities(List<League> leagueEntities) {
        List<LeagueDto> leagueDtos = new ArrayList<>();
        for (League league : leagueEntities) {
            leagueDtos.add(createLeagueDto(league));
        }
        return leagueDtos;
    }

    public static LeagueDto leagueDtoFromEntity(League league) {
        return createLeagueDto(league);
    }

    public static List<TeamDto> teamDtosFromEntities(List<Team> teamEntities) {
        List<TeamDto> teamDtos = new ArrayList<>();
        for (Team team : teamEntities) {
            teamDtos.add(createTeamDto(team));
        }
        return teamDtos;
    }

    public static TeamDto teamDtoFromEntity(Team teamEntity) {
        return createTeamDto(teamEntity);
    }

    public static LiveStatusDto liveStatusDtoFromEntity(LiveStatus liveStatus) {
        return new LiveStatusDto(liveStatus.getLongStatus(), liveStatus.getShortStatus(), liveStatus.getElapsed(), liveStatus.getHomeScore(), liveStatus.getAwayScore());
    }

    public static PlayerDto playerDtoFromEntity(Player player) {
        return new PlayerDto(player.getId(), player.getName(), player.getKoreanName(), player.getPhotoUrl(), player.getPosition());
    }

    public static List<PlayerDto> playerDtosFromEntities(List<Player> squadEntities) {
        List<PlayerDto> playerDtos = new ArrayList<>();
        for (Player player : squadEntities) {
            playerDtos.add(createPlayerDto(player));
        }
        return playerDtos;
    }

    public static FixtureInfoDto fixtureInfoDtoFromEntity(Fixture fixture) {
        return createFixtureInfoDto(fixture);
    }

    public static List<FixtureInfoDto> fixtureInfoDtosFromEntities(List<Fixture> fixtures) {
        List<FixtureInfoDto> fixtureInfoDtos = new ArrayList<>();
        for (Fixture fixture : fixtures) {
            fixtureInfoDtos.add(createFixtureInfoDto(fixture));
        }
        return fixtureInfoDtos;
    }

    public static FixtureWithLineupDto fixtureWithLineupDtoFromEntity(Fixture findFixture) {
        MatchLineup homeLineup = findFixture.getLineups().stream().filter(lineup -> lineup.getTeam().equals(findFixture.getHomeTeam())).findFirst().orElseThrow();
        MatchLineup awayLineup = findFixture.getLineups().stream().filter(lineup -> lineup.getTeam().equals(findFixture.getAwayTeam())).findFirst().orElseThrow();
        return new FixtureWithLineupDto(fixtureInfoDtoFromEntity(findFixture), lineupDtoFromEntity(homeLineup), lineupDtoFromEntity(awayLineup));
    }

    public static FixtureWithLineupDto fixtureWithEmptyLineupDtoFromEntity(Fixture findFixture) {
        return new FixtureWithLineupDto(fixtureInfoDtoFromEntity(findFixture), emptyLineupDtoFromTeamEntity(findFixture.getHomeTeam()), emptyLineupDtoFromTeamEntity(findFixture.getAwayTeam()));
    }

    public static LineupDto emptyLineupDtoFromTeamEntity(Team team) {
        return new LineupDto(createLineupTeamDto(team), "", List.of());
    }

    public static LineupDto lineupDtoFromEntity(MatchLineup matchLineup) {
        return new LineupDto(createLineupTeamDto(matchLineup.getTeam()), matchLineup.getFormation(), lineupPlayerDtoListFromEntities(matchLineup.getMatchPlayers()));
    }

    public static List<FixtureEventWithPlayerDto> fixtureEventDtosFromEntities(List<FixtureEvent> fixtureEvents) {
        List<FixtureEventWithPlayerDto> fixtureEventDtos = new ArrayList<>();
        for (FixtureEvent fixtureEvent : fixtureEvents) {
            fixtureEventDtos.add(createFixtureEventDto(fixtureEvent));
        }
        return fixtureEventDtos;
    }

    public static ExternalApiStatusDto apiStatusDtoFromEntity(ApiStatus apiStatus) {
        return new ExternalApiStatusDto(apiStatus.current(), apiStatus.minuteLimit(), apiStatus.minuteRemaining(), apiStatus.dayLimit(), apiStatus.dayRemaining(), apiStatus.active());
    }

    private static void assertStatsMatchFixtureTeams(Fixture fixture, @Nullable TeamStatistics homeStats, @Nullable TeamStatistics awayStats) {
        if (homeStats != null && homeStats.getTeam().getId() != fixture.getHomeTeam().getId()) {
            log.error("homeStats teamId={} is not same as fixture home teamId={}", homeStats.getTeam().getId(), fixture.getHomeTeam().getId());
            throw new IllegalArgumentException("homeStats teamId=" + homeStats.getTeam().getId() + " is not same as fixture home teamId=" + fixture.getHomeTeam().getId());
        }
        if (awayStats != null && awayStats.getTeam().getId() != fixture.getAwayTeam().getId()) {
            log.error("awayStats teamId={} is not same as fixture away teamId={}", awayStats.getTeam().getId(), fixture.getAwayTeam().getId());
            throw new IllegalArgumentException("awayStats teamId=" + awayStats.getTeam().getId() + " is not same as fixture away teamId=" + fixture.getAwayTeam().getId());
        }
    }

    private static MatchStatisticsDto createMatchStatisticsDTO(MatchStatisticsDto.MatchStatsFixture matchStatsFixture, MatchStatisticsDto.MatchStatsLiveStatus matchStatsLiveStatus, MatchStatisticsDto.MatchStatsTeam homeDTO, MatchStatisticsDto.MatchStatsTeam awayDTO, MatchStatisticsDto.MatchStatsTeamStatistics homeStatisticsDTO, MatchStatisticsDto.MatchStatsTeamStatistics awayStatisticsDTO, List<MatchStatisticsDto.MatchStatsPlayers> homePlayerStatisticsDTO, List<MatchStatisticsDto.MatchStatsPlayers> awayPlayerStatisticsDTO) {
        return new MatchStatisticsDto(matchStatsFixture, matchStatsLiveStatus, homeDTO, awayDTO, homeStatisticsDTO, awayStatisticsDTO, homePlayerStatisticsDTO, awayPlayerStatisticsDTO);
    }

    private static MatchStatisticsDto.MatchStatsFixture createFixtureDTO(Fixture fixture) {
        return new MatchStatisticsDto.MatchStatsFixture(fixture.getFixtureId(), fixture.getReferee(), fixture.getDate(), fixture.getTimezone(), fixture.getTimestamp(), fixture.isAvailable(), fixture.getRound());
    }

    private static MatchStatisticsDto.MatchStatsLiveStatus createLiveStatusDTO(LiveStatus liveStatus) {
        return new MatchStatisticsDto.MatchStatsLiveStatus(liveStatus.getLongStatus(), liveStatus.getShortStatus(), liveStatus.getElapsed(), liveStatus.getHomeScore(), liveStatus.getAwayScore());
    }

    private static MatchStatisticsDto.MatchStatsTeam createTeamDTO(Team away) {
        return new MatchStatisticsDto.MatchStatsTeam(away.getId(), away.getName(), away.getKoreanName(), away.getLogo());
    }

    private static List<MatchStatisticsDto.MatchStatsPlayers> createListOfMatchPlayerStatisticsDTO(List<MatchPlayer> playerStats) {
        List<MatchStatisticsDto.MatchStatsPlayers> list = new ArrayList<>();
        for (MatchPlayer mp : playerStats) {
            PlayerStatistics ps = mp.getPlayerStatistics();
            if (ps == null) {
                continue;
            }
            MatchStatisticsDto.MatchStatsPlayerStatistics stats = createPlayerStatisticsDto(ps);
            MatchStatisticsDto.MatchStatsPlayers dto;
            if (mp.getPlayer() == null) {
                dto = createUnregisteredPlayerStatisticsDTO(mp, stats);
            } else {
                dto = createRegisteredPlayerStatisticsDTO(mp, stats);
            }
            list.add(dto);
        }
        return list;
    }

    private static MatchStatisticsDto.MatchStatsPlayers createRegisteredPlayerStatisticsDTO(MatchPlayer mp, MatchStatisticsDto.MatchStatsPlayerStatistics stats) {
        Player player = mp.getPlayer();
        if (player == null) {
            log.error("Player is null. MatchPlayer: {}", mp);
            throw new IllegalArgumentException("Player is null. MatchPlayer: " + mp);
        }
        return new MatchStatisticsDto.MatchStatsPlayers(player.getId(), player.getName(), player.getKoreanName(), player.getPhotoUrl(), player.getNumber(), mp.getPosition(), mp.getSubstitute(), stats, null);
    }

    private static MatchStatisticsDto.MatchStatsPlayers createUnregisteredPlayerStatisticsDTO(MatchPlayer mp, MatchStatisticsDto.MatchStatsPlayerStatistics stats) {
        return new MatchStatisticsDto.MatchStatsPlayers(null, mp.getUnregisteredPlayerName(), "", "", mp.getUnregisteredPlayerNumber(), mp.getPosition(), mp.getSubstitute(), stats, mp.getTemporaryId());
    }

    private static MatchStatisticsDto.MatchStatsPlayerStatistics createPlayerStatisticsDto(PlayerStatistics ps) {
        return new MatchStatisticsDto.MatchStatsPlayerStatistics(ps.getMinutesPlayed(), ps.getPosition(), ps.getRating(), ps.getCaptain(), ps.getSubstitute(), ps.getShotsTotal(), ps.getShotsOn(), ps.getGoals(), ps.getGoalsConceded(), ps.getAssists(), ps.getSaves(), ps.getPassesTotal(), ps.getPassesKey(), ps.getPassesAccuracy(), ps.getTacklesTotal(), ps.getInterceptions(), ps.getDuelsTotal(), ps.getDuelsWon(), ps.getDribblesAttempts(), ps.getDribblesSuccess(), ps.getFoulsCommitted(), ps.getFoulsDrawn(), ps.getYellowCards(), ps.getRedCards(), ps.getPenaltiesScored(), ps.getPenaltiesMissed(), ps.getPenaltiesSaved());
    }

    private static MatchStatisticsDto.MatchStatsTeamStatistics createTeamStatisticsDTO(@Nullable TeamStatistics homeStats) {
        List<MatchStatisticsDto.MatchStatsXg> xgDtoList = new ArrayList<>();
        if (homeStats == null) {
            return new MatchStatisticsDto.MatchStatsTeamStatistics(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, List.of());
        }
        List<ExpectedGoals> expectedGoalsList = homeStats.getExpectedGoalsList();
        for (ExpectedGoals eg : expectedGoalsList) {
            xgDtoList.add(new MatchStatisticsDto.MatchStatsXg(eg.getElapsed(), eg.getXg()));
        }
        return new MatchStatisticsDto.MatchStatsTeamStatistics(homeStats.getShotsOnGoal(), homeStats.getShotsOffGoal(), homeStats.getTotalShots(), homeStats.getBlockedShots(), homeStats.getShotsInsideBox(), homeStats.getShotsOutsideBox(), homeStats.getFouls(), homeStats.getCornerKicks(), homeStats.getOffsides(), homeStats.getBallPossession(), homeStats.getYellowCards(), homeStats.getRedCards(), homeStats.getGoalkeeperSaves(), homeStats.getTotalPasses(), homeStats.getPassesAccurate(), homeStats.getPassesAccuracyPercentage(), homeStats.getGoalsPrevented(), xgDtoList);
    }

    private static LeagueDto createLeagueDto(League league) {
        return new LeagueDto(league.getLeagueId(), league.getName(), league.getKoreanName(), league.getLogo(), league.isAvailable(), league.getCurrentSeason());
    }

    private static TeamDto createTeamDto(Team team) {
        return new TeamDto(team.getId(), team.getName(), team.getKoreanName(), team.getLogo());
    }

    private static PlayerDto createPlayerDto(Player player) {
        return new PlayerDto(player.getId(), player.getName(), player.getKoreanName(), player.getPhotoUrl(), player.getPosition());
    }

    private static FixtureInfoDto createFixtureInfoDto(Fixture fixture) {
        return new FixtureInfoDto(fixture.getFixtureId(), fixture.getReferee(), fixture.getRound(), fixture.getTimezone(), ZonedDateTime.of(fixture.getDate(), ZoneId.of(fixture.getTimezone())), fixture.getTimestamp(), fixture.isAvailable(), liveStatusDtoFromEntity(fixture.getLiveStatus()), leagueDtoFromEntity(fixture.getLeague()), teamDtoFromEntity(fixture.getHomeTeam()), teamDtoFromEntity(fixture.getAwayTeam()));
    }

    private static LineupDto.LineupTeamDto createLineupTeamDto(Team team) {
        return new LineupDto.LineupTeamDto(team.getId(), team.getName(), team.getKoreanName(), team.getLogo());
    }

    private static List<LineupDto.LineupPlayer> lineupPlayerDtoListFromEntities(List<MatchPlayer> matchPlayers) {
        List<LineupDto.LineupPlayer> lineupPlayerDtos = new ArrayList<>();
        for (MatchPlayer matchPlayer : matchPlayers) {
            lineupPlayerDtos.add(createLineupPlayerDto(matchPlayer));
        }
        return lineupPlayerDtos;
    }

    private static LineupDto.LineupPlayer createLineupPlayerDto(MatchPlayer matchPlayer) {
        if (isUnregisteredPlayer(matchPlayer)) {
            return new LineupDto.LineupPlayer(null, matchPlayer.getUnregisteredPlayerName(), "", "", matchPlayer.getPosition(), null, matchPlayer.getTemporaryId(), matchPlayer.getUnregisteredPlayerName(), matchPlayer.getUnregisteredPlayerNumber(), matchPlayer.getGrid(), matchPlayer.getSubstitute());
        } else {
            assert matchPlayer.getPlayer() != null;
            return new LineupDto.LineupPlayer(matchPlayer.getPlayer().getId(), matchPlayer.getPlayer().getName(), matchPlayer.getPlayer().getKoreanName(), matchPlayer.getPlayer().getPhotoUrl(), matchPlayer.getPosition(), matchPlayer.getPlayer().getNumber(), null, null, null, matchPlayer.getGrid(), matchPlayer.getSubstitute());
        }
    }

    private static FixtureEventWithPlayerDto createFixtureEventDto(FixtureEvent fixtureEvent) {
        return new FixtureEventWithPlayerDto(fixtureEvent.getSequence(), fixtureEvent.getTimeElapsed(), fixtureEvent.getExtraTime(), createEventTeamDto(fixtureEvent.getTeam()), createEventPlayerDto(fixtureEvent.getPlayer()), createEventPlayerDto(fixtureEvent.getAssist()), fixtureEvent.getType().toString(), fixtureEvent.getDetail(), fixtureEvent.getComments());
    }

    private static FixtureEventWithPlayerDto.EventPlayerDto createEventPlayerDto(@Nullable MatchPlayer player) {
        if (player == null) {
            return null;
        }
        if (isUnregisteredPlayer(player)) {
            return new FixtureEventWithPlayerDto.EventPlayerDto(null, player.getUnregisteredPlayerName(), "", player.getUnregisteredPlayerNumber(), player.getTemporaryId() != null ? player.getTemporaryId().toString() : null);
        } else {
            assert player.getPlayer() != null;
            return new FixtureEventWithPlayerDto.EventPlayerDto(player.getPlayer().getId(), player.getPlayer().getName(), player.getPlayer().getKoreanName(), player.getPlayer().getNumber(), null);
        }
    }

    private static FixtureEventWithPlayerDto.EventTeamDto createEventTeamDto(Team team) {
        return new FixtureEventWithPlayerDto.EventTeamDto(team.getId(), team.getName(), team.getKoreanName());
    }

    private static boolean isUnregisteredPlayer(MatchPlayer matchPlayer) {
        return matchPlayer.getPlayer() == null;
    }
}
