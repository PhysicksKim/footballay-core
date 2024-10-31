package com.gyechunsik.scoreboard.domain.football.external.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchLineup;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchPlayer;
import com.gyechunsik.scoreboard.domain.football.persistence.live.PlayerStatistics;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.MatchLineupRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.MatchPlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.PlayerStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse.*;
import static com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse._FixturePlayers.*;

@RequiredArgsConstructor
@Transactional
@Slf4j
@Service
public class PlayerStatisticsService {

    private final TeamRepository teamRepository;
    private final FixtureRepository fixtureRepository;
    private final PlayerStatisticsRepository playerStatisticsRepository;
    private final MatchLineupRepository matchLineupRepository;
    private final MatchPlayerRepository matchPlayerRepository;

    public void savePlayerStatistics(FixtureSingleResponse response) {
        // 1) Response 에서 데이터 추출
        _FixtureSingle fixtureSingle = response.getResponse().get(0);
        _Home home = fixtureSingle.getTeams().getHome();
        _Away away = fixtureSingle.getTeams().getAway();

        Long fixtureId = fixtureSingle.getFixture().getId();
        Long homeId = home.getId();
        Long awayId = away.getId();

        // 2) 선수 통계 정보 추출
        List<_FixturePlayers> bothTeamPlayerStatistics = fixtureSingle.getPlayers();
        List<_PlayerStatistics> homePlayerStatisticsList = extractTeamPlayerStatistics(bothTeamPlayerStatistics, homeId);
        List<_PlayerStatistics> awayPlayerStatisticsList = extractTeamPlayerStatistics(bothTeamPlayerStatistics, awayId);

        // 3) 필요 엔티티 - 경기일정, 팀 조회
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("fixtureId=" + fixtureId + " 에 해당하는 경기 정보가 없습니다."));
        Team homeTeam = teamRepository.findById(homeId)
                .orElseThrow(() -> new IllegalArgumentException("homeId=" + homeId + " 에 해당하는 팀 정보가 없습니다."));
        Team awayTeam = teamRepository.findById(awayId)
                .orElseThrow(() -> new IllegalArgumentException("awayId=" + awayId + " 에 해당하는 팀 정보가 없습니다."));

        // 4) 필요 엔티티 - 선수들 조회
        List<MatchLineup> lineups = matchLineupRepository.findAllByFixture(fixture);
        if (lineups.size() != 2) {
            log.error("fixtureId={} 에 해당하는 경기의 라인업 정보가 올바르지 않습니다. size={}", fixtureId, lineups.size());
            return;
        }

        Optional<MatchLineup> optionalHomeLineup
                = lineups.stream().filter(lineup -> lineup.getTeam().equals(homeTeam)).findFirst();
        Optional<MatchLineup> optionalAwayLineup
                = lineups.stream().filter(lineup -> lineup.getTeam().equals(awayTeam)).findFirst();

        if (optionalHomeLineup.isEmpty() || optionalAwayLineup.isEmpty()) {
            log.error("fixtureId={} 에 해당하는 경기의 라인업 정보가 올바르지 않습니다. lineup 에 홈/어웨이 팀의 라인업이 존재하지 않습니다. home={}, away={}",
                    fixtureId, optionalHomeLineup.isPresent(), optionalAwayLineup.isPresent());
            return;
        }

        // TODO : 여기서 getMatchPlayers() 할 때 N+1 문제가 발생할 수 있음. 테스트 필요
        List<MatchPlayer> homeMatchPlayers = optionalHomeLineup.get().getMatchPlayers();
        List<MatchPlayer> awayMatchPlayers = optionalAwayLineup.get().getMatchPlayers();

        // 5) 선수 통계 정보 저장
        savePlayerStatistics(homeMatchPlayers, homePlayerStatisticsList);
        savePlayerStatistics(awayMatchPlayers, awayPlayerStatisticsList);
    }

    private void savePlayerStatistics(
            List<MatchPlayer> matchPlayers,
            List<_PlayerStatistics> playerStatisticsList) {
        // 6) Registered 여부에 따라 따로 동작하도록 하기 위해 Map 으로 나누기
        Map<Long, MatchPlayer> registeredPlayerMap = matchPlayers.stream()
                .filter(mp -> mp.getPlayer() != null)
                .collect(Collectors.toMap(mp -> mp.getPlayer().getId(), Function.identity()));
        Map<String, MatchPlayer> unregisteredPlayerMap = matchPlayers.stream()
                .filter(mp -> mp.getPlayer() == null && StringUtils.hasText(mp.getUnregisteredPlayerName()))
                .collect(Collectors.toMap(MatchPlayer::getUnregisteredPlayerName, Function.identity()));

        Map<Long, _PlayerStatistics> registeredPlayerStatsMap = playerStatisticsList.stream()
                .filter(ps -> ps.getPlayer() != null && ps.getPlayer().getId() != null)
                .collect(Collectors.toMap(ps -> ps.getPlayer().getId(), Function.identity()));
        Map<String, _PlayerStatistics> unregisteredPlayerStatsMap = playerStatisticsList.stream()
                .filter(ps -> (ps.getPlayer() == null || ps.getPlayer().getId() == null) && StringUtils.hasText(ps.getPlayer().getName()))
                .collect(Collectors.toMap(ps -> ps.getPlayer().getName(), Function.identity()));

        // 7) unregistered/registered PlayerMap 에 대한 통계 정보 업데이트
        List<PlayerStatistics> createdPlayerStatistics = new ArrayList<>();
        List<MatchPlayer> updatedMatchPlayers = new ArrayList<>();
        processPlayerMap(registeredPlayerMap, registeredPlayerStatsMap, createdPlayerStatistics, updatedMatchPlayers);
        processPlayerMap(unregisteredPlayerMap, unregisteredPlayerStatsMap, createdPlayerStatistics, updatedMatchPlayers);

        // 8) 새롭게 생성된 playerStatistics entity save
        List<PlayerStatistics> savedPlayerStatistics = playerStatisticsRepository.saveAll(createdPlayerStatistics);
        List<MatchPlayer> savedMatchPlayers = matchPlayerRepository.saveAll(updatedMatchPlayers);

        // 9) 새롭게 생성된 PlayerStatistics logging
        log.info("새롭게 생성된 PlayerStatistics entity size: {}", savedPlayerStatistics.size());
    }

    private void processPlayerMap(Map<?, MatchPlayer> playerMap,
                                  Map<?, _PlayerStatistics> statsMap,
                                  List<PlayerStatistics> createdPlayerStatistics,
                                  List<MatchPlayer> updatedMatchPlayers) {
        for (Map.Entry<?, MatchPlayer> entry : playerMap.entrySet()) {
            MatchPlayer mp = entry.getValue();
            _PlayerStatistics playerStatistics = statsMap.get(entry.getKey());
            saveOrUpdatePlayerStatistics(createdPlayerStatistics, updatedMatchPlayers, mp, playerStatistics);
        }
    }

    private void saveOrUpdatePlayerStatistics(List<PlayerStatistics> createdPlayerStatistics,
                                              List<MatchPlayer> updatedMatchPlayers,
                                              MatchPlayer mp,
                                              _PlayerStatistics playerStatistics) {
        _FixturePlayers._Statistics statsData = playerStatistics.getStatistics().get(0);
        PlayerStatistics statsEntity;
        if (mp.getPlayerStatistics() == null) {
            statsEntity = createAndUpdatePlayerStatistics(statsData, mp);
            createdPlayerStatistics.add(statsEntity);
            mp.setPlayerStatistics(statsEntity);
            updatedMatchPlayers.add(mp);
        } else {
            statsEntity = mp.getPlayerStatistics();
            updatePlayerStatistics(statsData, statsEntity);
        }
    }

    private PlayerStatistics createAndUpdatePlayerStatistics(_FixturePlayers._Statistics statsData, MatchPlayer mp) {
        PlayerStatistics build = PlayerStatistics.builder().matchPlayer(mp).build();
        return updatePlayerStatistics(statsData, build);
    }


    private static PlayerStatistics updatePlayerStatistics(_FixturePlayers._Statistics statistics, PlayerStatistics entity) {
        entity.setMinutesPlayed(statistics.getGames().getMinutes());
        entity.setPosition(statistics.getGames().getPosition());
        entity.setRating(statistics.getGames().getRating());
        entity.setCaptain(statistics.getGames().getCaptain());
        entity.setSubstitute(statistics.getGames().getSubstitute());
        entity.setShotsTotal(statistics.getShots().getTotal());
        entity.setShotsOn(statistics.getShots().getOn());
        entity.setGoals(statistics.getGoals().getTotal());
        entity.setGoalsConceded(statistics.getGoals().getConceded());
        entity.setAssists(statistics.getGoals().getAssists());
        entity.setSaves(statistics.getGoals().getSaves());
        entity.setPassesTotal(statistics.getPasses().getTotal());
        entity.setPassesKey(statistics.getPasses().getKey());
        entity.setPassesAccuracy(statistics.getPasses().getAccuracy());
        entity.setTacklesTotal(statistics.getTackles().getTotal());
        entity.setInterceptions(statistics.getTackles().getInterceptions());
        entity.setDuelsTotal(statistics.getDuels().getTotal());
        entity.setDuelsWon(statistics.getDuels().getWon());
        entity.setDribblesAttempts(statistics.getDribbles().getAttempts());
        entity.setDribblesSuccess(statistics.getDribbles().getSuccess());
        entity.setFoulsCommitted(statistics.getFouls().getCommitted());
        entity.setFoulsDrawn(statistics.getFouls().getDrawn());
        entity.setYellowCards(statistics.getCards().getYellow());
        entity.setRedCards(statistics.getCards().getRed());
        entity.setPenaltiesScored(statistics.getPenalty().getScored());
        entity.setPenaltiesMissed(statistics.getPenalty().getMissed());
        entity.setPenaltiesSaved(statistics.getPenalty().getSaved());
        return entity;
    }

    private List<_PlayerStatistics> extractTeamPlayerStatistics(List<_FixturePlayers> bothTeamPlayerStatistics, Long teamId) {
        Optional<List<_PlayerStatistics>> findFirst = bothTeamPlayerStatistics.stream()
                .filter(playerStatistics -> playerStatistics.getTeam().getId().equals(teamId))
                .map(_FixturePlayers::getPlayers)
                .findFirst();
        if (findFirst.isEmpty()) {
            log.info("teamId={} 에 해당하는 선수 통계 정보가 없습니다.", teamId);
            return Collections.emptyList();
        }
        return findFirst.get();
    }

}
