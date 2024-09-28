package com.gyechunsik.scoreboard.domain.football.external.live;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.Player;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.entity.live.PlayerStatistics;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.PlayerStatisticsRepository;
import jakarta.annotation.Nullable;
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
    private final PlayerRepository playerRepository;
    private final PlayerStatisticsRepository playerStatisticsRepository;

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
        Map<Long, Player> homePlayerMap = extractPlayers(homePlayerStatisticsList);
        Map<Long, Player> awayPlayerMap = extractPlayers(awayPlayerStatisticsList);

        // 5) 선수 통계 정보 저장
        savePlayerStatistics(homePlayerStatisticsList, homePlayerMap, fixture, homeTeam);
        savePlayerStatistics(awayPlayerStatisticsList, awayPlayerMap, fixture, awayTeam);
    }

    private Map<Long, PlayerStatistics> savePlayerStatistics(
            List<_PlayerStatistics> playerStatisticsList,
            Map<Long, Player> playerMap,
            Fixture fixture,
            Team team) {
        // 6) 기존에 저장되어 있는 선수 통계 정보 조회
        Map<Long, PlayerStatistics> playerIdStatsMap = findAndMapPlayerStatistics(playerMap, fixture, team);

        // 7) API 응답에서 제공한 선수 통계 정보로 PlayerStatistics 엔티티 생성 또는 업데이트
        for (_PlayerStatistics playerStatistic : playerStatisticsList) {

            // 선수 엔티티, 선수 아이디, 선수 통계 정보 추출
            Long playerId = playerStatistic.getPlayer().getId();
            Player player = playerMap.get(playerId);
            _FixturePlayers._Statistics statistics = playerStatistic.getStatistics().get(0);

            // 선수 통계 정보 엔티티
            PlayerStatistics findPlayerStat = getOrCreatePlayerStatistics(playerIdStatsMap, fixture, team, player);

            updatePlayerStatistics(statistics, findPlayerStat);
            playerIdStatsMap.put(playerId, findPlayerStat);
        }

        // 8) 선수 통계 정보 엔티티들 저장
        List<PlayerStatistics> playerStatistics = playerStatisticsRepository.saveAll(playerIdStatsMap.values());

        // 9) 저장된 선수 로깅
        logSavedPlayers(playerStatistics);
        return playerIdStatsMap;
    }

    private static void logSavedPlayers(List<PlayerStatistics> playerStatistics) {
        String playerNames = playerStatistics.stream()
                .map(PlayerStatistics::getPlayer)
                .map(p -> StringUtils.hasText(p.getKoreanName())
                        ? p.getKoreanName() : p.getName())
                .collect(Collectors.joining(", "));
        log.info("선수 통계 정보 저장됨 : size={}, players=[{}]",
                playerStatistics.size(), playerNames);
    }

    private Map<Long, Player> extractPlayers(List<_PlayerStatistics> playerStatisticsList) {
        Map<Long, _Player> responsePlayerMap = extractResponsePlayerMap(playerStatisticsList);

        Map<Long, Player> playerMap = findPlayers(responsePlayerMap.keySet());
        Map<Long, _Player> notCachedPlayerMap = extractNotCachedPlayerMap(responsePlayerMap, playerMap);
        Map<Long, Player> alreadyCachedPlayerMap = extractAlreadyCachedPlayerMap(responsePlayerMap, playerMap);

        List<Player> newlyCachedPlayers = cacheNotSavedPlayers(notCachedPlayerMap);
        Map<Long, Player> newlyCachedPlayerMap = newlyCachedPlayers.stream().collect(Collectors.toMap(Player::getId, Function.identity()));

        Map<Long, Player> combinedMap = new HashMap<>();
        combinedMap.putAll(alreadyCachedPlayerMap);
        combinedMap.putAll(newlyCachedPlayerMap);
        return combinedMap;
    }

    private static Map<Long, _Player> extractResponsePlayerMap(List<_PlayerStatistics> playerStatisticsList) {
        return playerStatisticsList.stream()
                .map(_PlayerStatistics::getPlayer)
                .collect(Collectors.toMap(_Player::getId, Function.identity()));
    }

    private Map<Long, Player> extractAlreadyCachedPlayerMap(Map<Long, _Player> responsePlayerMap, Map<Long, Player> playerMap) {
        return responsePlayerMap.entrySet().stream()
                .filter(entry -> playerMap.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> playerMap.get(entry.getKey())));
    }

    private List<Player> cacheNotSavedPlayers(Map<Long, _Player> notSavedPlayerMap) {
        List<Player> list = notSavedPlayerMap.entrySet().stream()
                .map(entry -> Player.builder()
                        .id(entry.getKey())
                        .name(entry.getValue().getName())
                        .photoUrl(entry.getValue().getPhoto())
                        .build())
                .toList();
        List<Player> savedPlayers = playerRepository.saveAll(list);
        if(!savedPlayers.isEmpty()) {
            log.info("player statistics 에서 새롭게 캐싱한 선수 정보: {}", savedPlayers);
        }
        return savedPlayers;
    }

    private Map<Long, _Player> extractNotCachedPlayerMap(
            Map<Long, _Player> responsePlayerMap,
            Map<Long, Player> playerMap) {
        return responsePlayerMap.entrySet().stream()
                .filter(entry -> !playerMap.containsKey(entry.getKey()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private Map<Long, Player> findPlayers(Set<Long> ids) {
        return playerRepository.findAllById(ids).stream().collect(Collectors.toMap(Player::getId, Function.identity()));
    }

    /**
     *
     * @param playerMap
     * @param fixture
     * @param team
     * @return Map < playerId, PlayerStatistics >
     */
    private Map<Long, PlayerStatistics> findAndMapPlayerStatistics(Map<Long, Player> playerMap, Fixture fixture, Team team) {
        return playerStatisticsRepository.findByFixtureAndTeamAndPlayerIn(fixture, team, playerMap.values().stream().toList()).stream()
                .collect(Collectors.toMap(ps -> ps.getPlayer().getId(), Function.identity()));
    }

    private static PlayerStatistics getOrCreatePlayerStatistics(
            Map<Long, PlayerStatistics> playerIdStatsMap,
            Fixture fixture,
            Team team,
            Player player) {
        PlayerStatistics findPlayerStat = playerIdStatsMap.get(player.getId());
        if(findPlayerStat == null) {
            return PlayerStatistics.builder()
                    .fixture(fixture)
                    .team(team)
                    .player(player)
                    .build();
        } else {
            return findPlayerStat;
        }
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
        return bothTeamPlayerStatistics.stream()
                .filter(playerStatistics -> playerStatistics.getTeam().getId().equals(teamId))
                .map(_FixturePlayers::getPlayers)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("지정한 팀의 선수들 통계 정보가 없습니다."));
    }

}
