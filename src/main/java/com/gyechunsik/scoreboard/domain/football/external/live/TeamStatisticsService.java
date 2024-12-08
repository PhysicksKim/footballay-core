package com.gyechunsik.scoreboard.domain.football.external.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.ExpectedGoals;
import com.gyechunsik.scoreboard.domain.football.persistence.live.TeamStatistics;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse._FixtureSingle;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse._Statistics;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.ExpectedGoalsRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.FixtureEventRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.LiveStatusRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.TeamStatisticsRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse.*;

/**
 * External football Api 와 TeamStatistics 간의 의존성을 해결해주는 서비스입니다.
 */
@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class TeamStatisticsService {

    private final TeamStatisticsRepository teamStatisticsRepository;

    private final TeamRepository teamRepository;
    private final FixtureRepository fixtureRepository;

    private static final List<String> FINISHED_STATUSES
            = List.of("TBD", "FT", "AET", "PEN", "PST", "CANC", "ABD", "AWD", "WO");
    private final ExpectedGoalsRepository expectedGoalsRepository;

    private Map<Long, _Statistics> mapStatisticsByTeamId(List<_Statistics> statisticsList) {
        return statisticsList.stream()
                .collect(Collectors.toMap(stat -> stat.getTeam().getId(), stat -> stat));
    }

    /**
     * <pre>
     * 기존에 저장된 팀통계가 있으면 업데이트하고 없으면 새로 생성해서 저장합니다.
     * ExpectedGoals 값 저장도 담당합니다.
     * </pre>
     * @param response
     * @see TeamStatistics
     * @see com.gyechunsik.scoreboard.domain.football.persistence.live.ExpectedGoals
     */
    public void saveTeamStatistics(FixtureSingleResponse response) {
        _FixtureSingle fixtureSingle = response.getResponse().get(0);
        _Home home = fixtureSingle.getTeams().getHome();
        _Away away = fixtureSingle.getTeams().getAway();

        List<_Statistics> statisticsList = fixtureSingle.getStatistics();
        if (statisticsList == null || statisticsList.size() < 2) {
            log.info("Insufficient statistics data. listSize={}", statisticsList == null ? "null" : statisticsList.size());
            return;
        }

        Map<Long, _Statistics> statisticsMap = mapStatisticsByTeamId(statisticsList);
        _Statistics homeStatisticsResponse = statisticsMap.get(home.getId());
        _Statistics awayStatisticsResponse = statisticsMap.get(away.getId());
        if (homeStatisticsResponse == null || awayStatisticsResponse == null) {
            log.error("Home or away statistics not matched with home or away team. Home ID: {}, Away ID: {}",
                    home.getId(), away.getId());
            throw new IllegalArgumentException("Home or away statistics not matched with home or away team");
        }

        // Find exist entities
        Fixture fixture = fixtureRepository.findById(fixtureSingle.getFixture().getId())
                .orElseThrow(() -> new IllegalArgumentException("Fixture not found"));
        Team homeTeam = teamRepository.findById(home.getId())
                .orElseThrow(() -> new IllegalArgumentException("Home team not found"));
        Team awayTeam = teamRepository.findById(away.getId())
                .orElseThrow(() -> new IllegalArgumentException("Away team not found"));

        // 팀 통계 기존에 있는지 fixture and team 으로 찾음
        Optional<TeamStatistics> optionalHomeStatistics = teamStatisticsRepository.findByFixtureAndTeam(fixture, homeTeam);
        Optional<TeamStatistics> optionalAwayStatistics = teamStatisticsRepository.findByFixtureAndTeam(fixture, awayTeam);

        Integer elapsed = fixtureSingle.getFixture().getStatus().getElapsed();
        TeamStatistics homeStatistics, awayStatistics;
        if (optionalAwayStatistics.isEmpty() || optionalHomeStatistics.isEmpty()) {
            homeStatistics = createTeamStatistics(homeStatisticsResponse, fixture, homeTeam);
            awayStatistics = createTeamStatistics(awayStatisticsResponse, fixture, awayTeam);
            log.info("new team statistics saved: elapsed : {}, home: {}, away: {}", elapsed, homeStatistics, awayStatistics);
        } else {
            homeStatistics = optionalHomeStatistics.get();
            awayStatistics = optionalAwayStatistics.get();
            updateTeamStatistics(homeStatisticsResponse, homeStatistics);
            updateTeamStatistics(awayStatisticsResponse, awayStatistics);
            log.info("team statistics updated: elapsed : {}, home: {}, away: {}", elapsed, homeStatistics, awayStatistics);
        }

        String homeXgValue = extractXgValue(homeStatisticsResponse);
        String awayXgValue = extractXgValue(awayStatisticsResponse);

        addOrUpdateXgToList(homeXgValue, elapsed, homeStatistics);
        addOrUpdateXgToList(awayXgValue, elapsed, awayStatistics);

        teamStatisticsRepository.save(homeStatistics);
        teamStatisticsRepository.save(awayStatistics);
    }

    private void addOrUpdateXgToList(String xgValue, Integer elapsed, TeamStatistics teamStatistics) {
        if (xgValue == null) {
            log.info("Expected goals data not found for team : {} {}", teamStatistics.getTeam().getId(), teamStatistics.getTeam().getName());
            return;
        }

        List<ExpectedGoals> xgList = teamStatistics.getExpectedGoalsList();
        for (ExpectedGoals xg : xgList) {
            if (xg.getElapsed().equals(elapsed)) {
                xg.setXg(xgValue);
                return;
            }
        }

        ExpectedGoals newXg = ExpectedGoals.builder()
                .elapsed(elapsed)
                .xg(xgValue)
                .teamStatistics(teamStatistics)
                .build();
        xgList.add(newXg);
        log.info("ADD NEW XG: {}", newXg);
    }

    private void addXgToList(List<ExpectedGoals> xgList, _Statistics homeStatisticsResponse, Integer elapsed, TeamStatistics teamStatistics) {
        String xgValue = extractXgValue(homeStatisticsResponse);

        if (xgValue == null) {
            log.warn("Expected goals data not found for team {}", homeStatisticsResponse.getTeam().getId());
            return;
        }

        ExpectedGoals xg = ExpectedGoals.builder()
                .elapsed(elapsed)
                .xg(xgValue)
                .teamStatistics(teamStatistics)
                .build();
        xgList.add(xg);
    }

    private TeamStatistics createTeamStatistics(_Statistics statistics, Fixture fixture, Team team) {
        TeamStatistics teamStatistics = TeamStatistics.builder()
                .fixture(fixture)
                .team(team)
                .build();

        updateTeamStatistics(statistics, teamStatistics);
        return teamStatistics;
    }

    private void updateTeamStatistics(_Statistics statistics, TeamStatistics teamStatistics) {
        for (_Statistics._StatisticsData statData : statistics.getStatistics()) {
            String statType = normalizeStatType(statData.getType());
            String value = statData.getValue();

            switch (statType) {
                case "shotsongoal":
                    teamStatistics.setShotsOnGoal(parseIntegerValue(value));
                    break;
                case "shotsoffgoal":
                    teamStatistics.setShotsOffGoal(parseIntegerValue(value));
                    break;
                case "totalshots":
                    teamStatistics.setTotalShots(parseIntegerValue(value));
                    break;
                case "blockedshots":
                    teamStatistics.setBlockedShots(parseIntegerValue(value));
                    break;
                case "shotsinsidebox":
                    teamStatistics.setShotsInsideBox(parseIntegerValue(value));
                    break;
                case "shotsoutsidebox":
                    teamStatistics.setShotsOutsideBox(parseIntegerValue(value));
                    break;
                case "fouls":
                    teamStatistics.setFouls(parseIntegerValue(value));
                    break;
                case "cornerkicks":
                    teamStatistics.setCornerKicks(parseIntegerValue(value));
                    break;
                case "offsides":
                    teamStatistics.setOffsides(parseIntegerValue(value));
                    break;
                case "ballpossession":
                    teamStatistics.setBallPossession(parsePercentageValue(value));
                    break;
                case "yellowcards":
                    teamStatistics.setYellowCards(parseIntegerValue(value));
                    break;
                case "redcards":
                    teamStatistics.setRedCards(parseIntegerValue(value));
                    break;
                case "goalkeepersaves":
                    teamStatistics.setGoalkeeperSaves(parseIntegerValue(value));
                    break;
                case "totalpasses":
                    teamStatistics.setTotalPasses(parseIntegerValue(value));
                    break;
                case "passesaccurate":
                    teamStatistics.setPassesAccurate(parseIntegerValue(value));
                    break;
                case "passes":
                    teamStatistics.setPassesAccuracyPercentage(parsePercentageValue(value));
                    break;
                case "expectedgoals":
                    continue;
                case "goalsprevented":
                    teamStatistics.setGoalsPrevented(parseIntegerValue(value));
                    break;
                default:
                    log.warn("Unknown statistics type '{}' value '{}' encountered for team ID {}", statType, value, teamStatistics.getTeam().getId());
                    break;
            }
        }
    }

    private String extractXgValue(_Statistics statisticsResponse) {
        for (_Statistics._StatisticsData statData : statisticsResponse.getStatistics()) {
            String statType = normalizeStatType(statData.getType());
            if ("expectedgoals".equals(statType)) {
                return statData.getValue();
            }
        }
        log.warn("xgValue: not found");
        return null;
    }

    private Integer parseIntegerValue(String value) {
        if (value == null) return null;
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.error("Failed to parse integer value: {}", value, e);
            return null;
        }
    }

    private Integer parsePercentageValue(String value) {
        if (value == null) return null;
        try {
            return Integer.parseInt(value.replace("%", ""));
        } catch (NumberFormatException e) {
            log.error("Failed to parse percentage value: {}", value, e);
            return null;
        }
    }

    /**
     * 팀 통계 타입을 정규화합니다. <br>
     * 모든 영문자를 소문자로 변경하고 알파벳 소문자 이외의 모든 문자(다른 언어, 공백, 특수문자 등) 제거합니다.
     * @param type
     * @return 소문자로 변환 후 알파벳만 남긴 문자열
     */
    private String normalizeStatType(String type) {
        return type.toLowerCase().replaceAll("[^a-z]", "");
    }

}