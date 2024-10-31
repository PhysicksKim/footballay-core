package com.gyechunsik.scoreboard.domain.football.dto;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class FootballDtoMapper {

    /**
     * <code>List<MatchPlayer></code> 의 MatchPlayer.PlayerStatistics 가 로딩된 채로 제공되어야 합니다.
     *
     * @param fixture
     * @param liveStatus
     * @param home
     * @param away
     * @param homeStats
     * @param awayStats
     * @param homePlayerStats
     * @param awayPlayerStats
     * @return
     */
    public static MatchStatisticsDTO matchStatisticsDTOFromEntity(
            Fixture fixture,
            LiveStatus liveStatus,
            Team home,
            Team away,
            @Nullable TeamStatistics homeStats,
            @Nullable TeamStatistics awayStats,
            List<MatchPlayer> homePlayerStats,
            List<MatchPlayer> awayPlayerStats
    ) {
        log.debug("fixtureId={} DTO mapper start", fixture.getFixtureId());
        MatchStatisticsDTO.FixtureDTO fixtureDTO = createFixtureDTO(fixture);

        MatchStatisticsDTO.LiveStatusDTO liveStatusDTO = createLiveStatusDTO(liveStatus);

        MatchStatisticsDTO.TeamDTO homeDTO = createTeamDTO(home);
        MatchStatisticsDTO.TeamDTO awayDTO = createTeamDTO(away);

        MatchStatisticsDTO.TeamStatisticsDTO homeStatisticsDTO = createTeamStatisticsDTO(homeStats);
        MatchStatisticsDTO.TeamStatisticsDTO awayStatisticsDTO = createTeamStatisticsDTO(awayStats);

        List<MatchStatisticsDTO.MatchPlayerStatisticsDTO> homePlayerStatisticsDTO = createListOfMatchPlayerStatisticsDTO(homePlayerStats);
        List<MatchStatisticsDTO.MatchPlayerStatisticsDTO> awayPlayerStatisticsDTO = createListOfMatchPlayerStatisticsDTO(awayPlayerStats);

        MatchStatisticsDTO dto = createMatchStatisticsDTO(fixtureDTO, liveStatusDTO, homeDTO, awayDTO, homeStatisticsDTO, awayStatisticsDTO, homePlayerStatisticsDTO, awayPlayerStatisticsDTO);
        log.debug("MatchStatisticsDTO: {}", dto);
        return dto;
    }

    private static MatchStatisticsDTO createMatchStatisticsDTO(
            MatchStatisticsDTO.FixtureDTO fixtureDTO,
            MatchStatisticsDTO.LiveStatusDTO liveStatusDTO,
            MatchStatisticsDTO.TeamDTO homeDTO,
            MatchStatisticsDTO.TeamDTO awayDTO,
            MatchStatisticsDTO.TeamStatisticsDTO homeStatisticsDTO,
            MatchStatisticsDTO.TeamStatisticsDTO awayStatisticsDTO,
            List<MatchStatisticsDTO.MatchPlayerStatisticsDTO> homePlayerStatisticsDTO,
            List<MatchStatisticsDTO.MatchPlayerStatisticsDTO> awayPlayerStatisticsDTO
    ) {
        return new MatchStatisticsDTO(
                fixtureDTO,
                liveStatusDTO,
                homeDTO,
                awayDTO,
                homeStatisticsDTO,
                awayStatisticsDTO,
                homePlayerStatisticsDTO,
                awayPlayerStatisticsDTO
        );
    }

    private static MatchStatisticsDTO.FixtureDTO createFixtureDTO(Fixture fixture) {
        return new MatchStatisticsDTO.FixtureDTO(
                fixture.getFixtureId(),
                fixture.getReferee(),
                fixture.getDate(),
                fixture.getTimezone(),
                fixture.getTimestamp(),
                fixture.isAvailable(),
                fixture.getRound()
        );
    }

    private static MatchStatisticsDTO.LiveStatusDTO createLiveStatusDTO(LiveStatus liveStatus) {
        return new MatchStatisticsDTO.LiveStatusDTO(
                liveStatus.getLongStatus(),
                liveStatus.getShortStatus(),
                liveStatus.getElapsed(),
                liveStatus.getHomeScore(),
                liveStatus.getAwayScore()
        );
    }

    private static MatchStatisticsDTO.TeamDTO createTeamDTO(Team away) {
        return new MatchStatisticsDTO.TeamDTO(
                away.getId(),
                away.getName(),
                away.getKoreanName(),
                away.getLogo()
        );
    }

    private static List<MatchStatisticsDTO.MatchPlayerStatisticsDTO> createListOfMatchPlayerStatisticsDTO(List<MatchPlayer> playerStats) {
        List<MatchStatisticsDTO.MatchPlayerStatisticsDTO> list = new ArrayList<>();
        for (MatchPlayer mp : playerStats) {
            PlayerStatistics ps = mp.getPlayerStatistics();
            if (ps == null) {
                log.error("PlayerStatistics is null. MatchPlayer: {}", mp);
                throw new IllegalArgumentException("PlayerStatistics is null. MatchPlayer: " + mp);
            }
            MatchStatisticsDTO.PlayerStatisticsDTO stats = createPlayerStatisticsDto(ps);

            MatchStatisticsDTO.MatchPlayerStatisticsDTO dto;
            if (mp.getPlayer() == null) {
                dto = createUnregisteredPlayerStatisticsDTO(mp, stats);
            } else {
                dto = createRegisteredPlayerStatisticsDTO(mp, stats);
            }
            list.add(dto);
        }
        return list;
    }

    private static MatchStatisticsDTO.MatchPlayerStatisticsDTO createRegisteredPlayerStatisticsDTO(MatchPlayer mp, MatchStatisticsDTO.PlayerStatisticsDTO stats) {
        Player player = mp.getPlayer();
        if (player == null) {
            log.error("Player is null. MatchPlayer: {}", mp);
            throw new IllegalArgumentException("Player is null. MatchPlayer: " + mp);
        }
        return new MatchStatisticsDTO.MatchPlayerStatisticsDTO(
                player.getId(),
                player.getName(),
                player.getKoreanName(),
                player.getPhotoUrl(),
                player.getNumber(),
                mp.getPosition(),
                mp.getSubstitute(),
                stats
        );
    }

    private static MatchStatisticsDTO.MatchPlayerStatisticsDTO createUnregisteredPlayerStatisticsDTO(MatchPlayer mp, MatchStatisticsDTO.PlayerStatisticsDTO stats) {
        return new MatchStatisticsDTO.MatchPlayerStatisticsDTO(
                null,
                mp.getUnregisteredPlayerName(),
                "",
                "",
                mp.getUnregisteredPlayerNumber(),
                mp.getPosition(),
                mp.getSubstitute(),
                stats
        );
    }

    private static MatchStatisticsDTO.PlayerStatisticsDTO createPlayerStatisticsDto(PlayerStatistics ps) {


        return new MatchStatisticsDTO.PlayerStatisticsDTO(
                ps.getMinutesPlayed(),
                ps.getPosition(),
                ps.getRating(),
                ps.getCaptain(),
                ps.getSubstitute(),
                ps.getShotsTotal(),
                ps.getShotsOn(),
                ps.getGoals(),
                ps.getGoalsConceded(),
                ps.getAssists(),
                ps.getSaves(),
                ps.getPassesTotal(),
                ps.getPassesKey(),
                ps.getPassesAccuracy(),
                ps.getTacklesTotal(),
                ps.getInterceptions(),
                ps.getDuelsTotal(),
                ps.getDuelsWon(),
                ps.getDribblesAttempts(),
                ps.getDribblesSuccess(),
                ps.getFoulsCommitted(),
                ps.getFoulsDrawn(),
                ps.getYellowCards(),
                ps.getRedCards(),
                ps.getPenaltiesScored(),
                ps.getPenaltiesMissed(),
                ps.getPenaltiesSaved()
        );
    }

    private static MatchStatisticsDTO.TeamStatisticsDTO createTeamStatisticsDTO(@Nullable TeamStatistics homeStats) {
        List<MatchStatisticsDTO.ExpectedGoalsDTO> xgDtoList = new ArrayList<>();
        if (homeStats == null) {
            return new MatchStatisticsDTO.TeamStatisticsDTO(
                    0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, List.of()
            );
        }

        List<ExpectedGoals> expectedGoalsList = homeStats.getExpectedGoalsList();
        for (ExpectedGoals eg : expectedGoalsList) {
            xgDtoList.add(new MatchStatisticsDTO.ExpectedGoalsDTO(eg.getElapsed(), eg.getXg()));
        }
        return new MatchStatisticsDTO.TeamStatisticsDTO(
                homeStats.getShotsOnGoal(),
                homeStats.getShotsOffGoal(),
                homeStats.getTotalShots(),
                homeStats.getBlockedShots(),
                homeStats.getShotsInsideBox(),
                homeStats.getShotsOutsideBox(),
                homeStats.getFouls(),
                homeStats.getCornerKicks(),
                homeStats.getOffsides(),
                homeStats.getBallPossession(),
                homeStats.getYellowCards(),
                homeStats.getRedCards(),
                homeStats.getGoalkeeperSaves(),
                homeStats.getTotalPasses(),
                homeStats.getPassesAccurate(),
                homeStats.getPassesAccuracyPercentage(),
                homeStats.getGoalsPrevented(),
                xgDtoList
        );
    }
}
