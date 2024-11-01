package com.gyechunsik.scoreboard.domain.football.external.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Player;
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
import org.jetbrains.annotations.NotNull;
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
    private final PlayerRepository playerRepository;

    /**
     * 반드시, MatchLineup 이 먼저 저장되어야지만 Fixture 에서 부터 선수별 통계를 저장할 수 있습니다. <br>
     * MatchLineup 이 저장되어 있지 않다면 캐싱하지 않고 넘어갑니다. <br>
     * @param response
     */
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
        MatchLineup homeMatchLineup = optionalHomeLineup.get();
        MatchLineup awayMatchLineup = optionalAwayLineup.get();

        // 5) 선수 통계 정보 저장
        savePlayerStatistics(homeMatchLineup, homePlayerStatisticsList);
        savePlayerStatistics(awayMatchLineup, awayPlayerStatisticsList);
    }

    private void savePlayerStatistics(
            MatchLineup matchLineup,
            List<_PlayerStatistics> playerStatisticsList) {
        // 6) Registered 여부에 따라 따로 동작하도록 하기 위해 Map 으로 나누기
        List<MatchPlayer> matchPlayers = matchLineup.getMatchPlayers();

        Map<Long, MatchPlayer> registeredPlayerMap = collectRegisteredPlayers(matchPlayers);
        Map<String, MatchPlayer> unregisteredPlayerMap = collectUnregisteredPlayers(matchPlayers);

        Map<Long, _PlayerStatistics> registeredPlayerStatsMap = collectRegisteredPlayerStats(playerStatisticsList);
        Map<String, _PlayerStatistics> unregisteredPlayerStatsMap = collectUnregisteredPlayerStats(playerStatisticsList);

        // 7) 혹시 통계 registeredPlayer 중 lineup 에 없는 선수가 있다면, 새롭게 캐싱하도록 함
        Set<Long> lineupRegisteredPlayerIds = new HashSet<>(registeredPlayerMap.keySet());
        Set<Long> statsRegisteredPlayerIds = new HashSet<>(registeredPlayerStatsMap.keySet());
        statsRegisteredPlayerIds.removeAll(lineupRegisteredPlayerIds);
        if (!statsRegisteredPlayerIds.isEmpty()) {
            log.warn("unexpected Players in players statistics. .size={}", statsRegisteredPlayerIds.size());
            Map<Long, _PlayerStatistics> unexpectedNewRegisteredPlayerStats = statsRegisteredPlayerIds.stream()
                    .map(registeredPlayerStatsMap::get)
                    .collect(Collectors.toMap(ps -> ps.getPlayer().getId(), Function.identity()));
            List<MatchPlayer> newlySavedUnexpectedNewRegisteredPlayers
                    = saveUnexpectedNewRegisteredPlayersInStatistics(matchLineup, unexpectedNewRegisteredPlayerStats);
            log.info("새롭게 저장된 unexpectedNewRegisteredPlayerStats.size={}", newlySavedUnexpectedNewRegisteredPlayers.size());
            registeredPlayerMap.putAll(newlySavedUnexpectedNewRegisteredPlayers.stream()
                    .filter(mp -> mp.getPlayer() != null)
                    .collect(Collectors.toMap(mp->mp.getPlayer().getId(), Function.identity())));
        }

        // 8) unregistered/registered PlayerMap 에 대한 통계 정보 업데이트
        List<PlayerStatistics> createdPlayerStatistics = new ArrayList<>();
        List<MatchPlayer> updatedMatchPlayers = new ArrayList<>();
        processPlayerMap(registeredPlayerMap, registeredPlayerStatsMap, createdPlayerStatistics, updatedMatchPlayers);
        processPlayerMap(unregisteredPlayerMap, unregisteredPlayerStatsMap, createdPlayerStatistics, updatedMatchPlayers);

        // 9) 새롭게 생성된 playerStatistics entity save
        List<PlayerStatistics> savedPlayerStatistics = playerStatisticsRepository.saveAll(createdPlayerStatistics);
        List<MatchPlayer> savedMatchPlayers = matchPlayerRepository.saveAll(updatedMatchPlayers);

        // 10) 새롭게 생성된 PlayerStatistics logging
        log.info("새롭게 생성된 PlayerStatistics entity size: {}", savedPlayerStatistics.size());
    }

    /**
     * MatchLineup 에서는 저장되지 않았으나 Statistics 에 새롭게 등장하고 있는 선수들을 캐싱하고 저장합니다. <br>
     * statsMap 에서 선수 정보를 추출하여 캐싱하고 저장합니다. <br>
     * @param matchLineup
     * @param statsMap
     * @return
     */
    private List<MatchPlayer> saveUnexpectedNewRegisteredPlayersInStatistics(MatchLineup matchLineup, Map<Long, _PlayerStatistics> statsMap) {
        List<MatchPlayer> matchPlayers = matchLineup.getMatchPlayers();
        List<MatchPlayer> newMatchPlayers = new ArrayList<>();

        for (_PlayerStatistics stat : statsMap.values()) {
            try {
                _Player statPlayer = stat.getPlayer();
                _FixturePlayers._Statistics statData = stat.getStatistics().get(0);
                Optional<Player> findPlayer = playerRepository.findById(statPlayer.getId());
                Player player;
                if(findPlayer.isEmpty()) {
                    Player build = Player.builder()
                            .id(statPlayer.getId())
                            .name(statPlayer.getName())
                            .photoUrl(statPlayer.getPhoto())
                            .number(statData.getGames().getNumber())
                            .build();
                    player = playerRepository.save(build);
                } else {
                    player = findPlayer.get();
                }

                MatchPlayer createdMatchPlayer = MatchPlayer.builder()
                        .matchLineup(matchLineup)
                        .player(player)
                        .substitute(false)
                        .position(statData.getGames().getPosition())
                        .build();
                MatchPlayer savedMatchPlayer = matchPlayerRepository.save(createdMatchPlayer);

                matchPlayers.add(savedMatchPlayer);
                newMatchPlayers.add(savedMatchPlayer);
            } catch (Exception e) {
                log.warn("Unexpected error occurred while saving new registered player when saving player statistics. Skip this playerStatisticsResponse={}", stat, e);
            }
        }
        matchLineup.setMatchPlayers(matchPlayers);
        matchLineupRepository.save(matchLineup);

        return newMatchPlayers;
    }

    private static @NotNull Map<String, _PlayerStatistics> collectUnregisteredPlayerStats(List<_PlayerStatistics> playerStatisticsList) {
        return playerStatisticsList.stream()
                .filter(ps -> (ps.getPlayer() == null || ps.getPlayer().getId() == null) && StringUtils.hasText(ps.getPlayer().getName()))
                .collect(Collectors.toMap(ps -> ps.getPlayer().getName(), Function.identity()));
    }

    private static @NotNull Map<Long, _PlayerStatistics> collectRegisteredPlayerStats(List<_PlayerStatistics> playerStatisticsList) {
        return playerStatisticsList.stream()
                .filter(ps -> ps.getPlayer() != null && ps.getPlayer().getId() != null)
                .collect(Collectors.toMap(ps -> ps.getPlayer().getId(), Function.identity()));
    }

    private static @NotNull Map<String, MatchPlayer> collectUnregisteredPlayers(List<MatchPlayer> matchPlayers) {
        return matchPlayers.stream()
                .filter(mp -> mp.getPlayer() == null && StringUtils.hasText(mp.getUnregisteredPlayerName()))
                .collect(Collectors.toMap(MatchPlayer::getUnregisteredPlayerName, Function.identity()));
    }

    private static @NotNull Map<Long, MatchPlayer> collectRegisteredPlayers(List<MatchPlayer> matchPlayers) {
        return matchPlayers.stream()
                .filter(mp -> mp.getPlayer() != null)
                .collect(Collectors.toMap(mp -> mp.getPlayer().getId(), Function.identity()));
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
        if(playerStatistics == null || playerStatistics.getStatistics() == null || playerStatistics.getStatistics().isEmpty()) {
            log.warn("playerStatistics 가 null 입니다. playerStatistics={}, mp={}", playerStatistics, mp);
            return;
        }
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
