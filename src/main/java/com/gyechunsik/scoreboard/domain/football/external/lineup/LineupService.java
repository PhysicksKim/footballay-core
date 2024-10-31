package com.gyechunsik.scoreboard.domain.football.external.lineup;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchLineup;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchPlayer;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.MatchLineupRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.MatchPlayerRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

import static com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse.*;

// TODO : lineup 에서 id = null 인 선수가 들어오면 제대로 null safe 하게 동작해서 라인업이 제대로 저장 되는지 테스트 작성 필요
@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class LineupService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final FixtureRepository fixtureRepository;
    private final MatchLineupRepository matchLineupRepository;
    private final MatchPlayerRepository matchPlayerRepository;

    private final EntityManager entityManager;

    public boolean hasLineupData(FixtureSingleResponse response) {
        return !response.getResponse().get(0).getLineups().isEmpty();
    }

    // fixtureId response 를 받아서 Entity 로 변환하고, 변환된 entity 를 저장
    public void saveLineup(FixtureSingleResponse response) {
        List<_FixtureSingle> fixtureSingleResponse = response.getResponse();
        if (fixtureSingleResponse.isEmpty()) {
            throw new IllegalArgumentException("API _Response 데이터가 없습니다.");
        }

        // 라인업을 위한 정보 추출
        _FixtureSingle fixtureSingle = fixtureSingleResponse.get(0);
        // _Fixture ID 추출
        Long fixtureIdResponse = fixtureSingle.getFixture().getId();
        // 리그 추출
        _League leagueResponse = fixtureSingle.getLeague();
        // 팀 추출
        _Home homeResponse = fixtureSingle.getTeams().getHome();
        _Away awayResponse = fixtureSingle.getTeams().getAway();
        List<_Lineups> lineups = fixtureSingle.getLineups();
        // 라인업 추출
        _Lineups homeLineupResponse = lineups.stream()
                .filter(lineup -> lineup.getTeam().getId().equals(homeResponse.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("홈팀 라인업 정보가 없습니다."));
        _Lineups awayLineupResponse = lineups.stream()
                .filter(lineup -> lineup.getTeam().getId().equals(awayResponse.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("일치하는 어웨이팀 라인업 정보가 없습니다."));

        // 라인업 정보를 이용해, (a) 존재하지 않는 player 를 저장하거나, (b) 존재한다면 number 정보를 업데이트 해줍니다.
        cacheAndUpdateFromLineupPlayers(homeLineupResponse);
        cacheAndUpdateFromLineupPlayers(awayLineupResponse);

        // 1. MatchLineup Entity 생성
        Fixture fixture = fixtureRepository.findById(fixtureIdResponse)
                .orElseThrow(() -> new IllegalArgumentException("경기 정보가 아직 캐싱되지 않았습니다."));
        Team homeTeam = teamRepository.findById(homeResponse.getId())
                .orElseThrow(() -> new IllegalArgumentException("홈팀이 아직 캐싱되지 않았습니다."));
        Team awayTeam = teamRepository.findById(awayResponse.getId())
                .orElseThrow(() -> new IllegalArgumentException("어웨이팀이 아직 캐싱되지 않았습니다."));
        MatchLineup homeLineup = MatchLineup.builder()
                .fixture(fixture)
                .formation(homeLineupResponse.getFormation())
                .team(homeTeam)
                .build();
        MatchLineup awayLineup = MatchLineup.builder()
                .fixture(fixture)
                .formation(awayLineupResponse.getFormation())
                .team(awayTeam)
                .build();
        MatchLineup homeMatchLineup = matchLineupRepository.save(homeLineup);
        MatchLineup awayMatchLineup = matchLineupRepository.save(awayLineup);

        // 2. MatchPlayer Entity 생성
        List<MatchPlayer> homeMatchPlayerList = buildAndSaveMatchPlayerEntity(homeLineupResponse, homeMatchLineup, false);
        List<MatchPlayer> homeSubstitutePlayerList = buildAndSaveMatchPlayerEntity(homeLineupResponse, homeMatchLineup, true);
        List<MatchPlayer> awayMatchPlayerList = buildAndSaveMatchPlayerEntity(awayLineupResponse, awayMatchLineup, false);
        List<MatchPlayer> awaySubstitutePlayerList = buildAndSaveMatchPlayerEntity(awayLineupResponse, awayMatchLineup, true);
        log.info("fixtureId={} 라인업 정보 저장 완료", fixtureIdResponse);
        log.info("홈팀 라인업 정보 저장 완료. fixtureId={}, teamId={}, startXI.size={}, subs.size={}",
                fixtureIdResponse, homeTeam.getId(), homeMatchPlayerList.size(), homeSubstitutePlayerList.size());
        log.info("어웨이팀 라인업 정보 저장 완료. fixtureId={}, teamId={}, startXI.size={}, subs.size={}",
                fixtureIdResponse, awayTeam.getId(), awayMatchPlayerList.size(), awaySubstitutePlayerList.size());
    }

    /**
     * 선발 라인업에 존재하지만 아직 캐싱되지 않은 선수들이 있다면 선수 정보를 캐싱합니다. <br>
     * 라인업 정보에 있는 데이터만으로 캐싱을 진행합니다. <br>
     * lineup 에 제공되지 않은 photo 와 같은 추가 정보는 별도로 캐싱을 진행해야 하며, <br>
     * TeamPlayer 와 같은 연관관계 맵핑 정보는 캐싱되지 않습니다. <br>
     * id 가 null 인 선수는 unregistered Player 로 MatchPlayer 에서 Player 연관관계를 맺지 않습니다.
     * @param lineupResponse 라인업 정보
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void cacheAndUpdateFromLineupPlayers(_Lineups lineupResponse) {
        List<_Lineups._StartPlayer> startXI = lineupResponse.getStartXI();
        List<_Lineups._StartPlayer> substitutes = lineupResponse.getSubstitutes();

        // id 가 null 인 선수는 무시하고 넘어갑니다. id 가 null 인 선수는 Player 로 저장되지 않고 MatchPlayer 에만 저장됩니다.
        Map<Long, _Lineups._StartPlayer> playerResponseMap = new HashMap<>();
        playerResponseMap.putAll(startXI.stream()
                .filter(player -> player.getPlayer().getId() != null)
                .collect(Collectors.toMap(player -> player.getPlayer().getId(), player -> player)));
        playerResponseMap.putAll(substitutes.stream()
                .filter(player -> player.getPlayer().getId() != null)
                .collect(Collectors.toMap(player -> player.getPlayer().getId(), player -> player)));

        Set<Long> playerIds = playerResponseMap.keySet();
        Set<Long> findPlayers = playerRepository.findAllById(playerIds).stream()
                .map(Player::getId)
                .collect(Collectors.toSet());

        Set<Long> existPlayerIds = playerIds.stream()
                .filter(findPlayers::contains)
                .collect(Collectors.toSet());
        updateExistPlayers(playerResponseMap, existPlayerIds);

        Set<Long> missingPlayerIds = playerIds.stream()
                .filter(playerId -> !findPlayers.contains(playerId))
                .collect(Collectors.toSet());
        if(missingPlayerIds.isEmpty()) {
            return;
        }
        List<_Lineups._StartPlayer> missingPlayers = playerResponseMap.entrySet().stream()
                .filter(entry -> missingPlayerIds.contains(entry.getKey()))
                .map(Map.Entry::getValue)
                .toList();
        convertAndCacheMissingPlayers(missingPlayers);
        log.info("라인업 선수 중 아직 캐싱되지 않은 선수를 저장했습니다. missingPlayers={}", missingPlayerIds);
    }

    /**
     * 기존에 존재하는 선수의 유니폼 번호를 업데이트 합니다. <br>
     * 기존에 저장된 번호가 없거나 다르다면 라인업에서 제공된 번호로 변경합니다. <br>
     * @param playerResponseMap
     * @param existPlayerIds
     */
    private void updateExistPlayers(Map<Long, _Lineups._StartPlayer> playerResponseMap, Set<Long> existPlayerIds) {
        List<Player> existPlayers = playerRepository.findAllById(existPlayerIds);
        existPlayers.forEach(player -> {
            _Lineups._StartPlayer playerResponse = playerResponseMap.get(player.getId());
            if (playerResponse.getPlayer().getNumber() != null && !Objects.equals(player.getNumber(), playerResponse.getPlayer().getNumber())) {
                player.setNumber(playerResponse.getPlayer().getNumber());
                log.info("선수 번호 정보를 업데이트 했습니다. player={}", player);
            }
        });
        playerRepository.saveAll(existPlayers);
    }

    private void convertAndCacheMissingPlayers(List<_Lineups._StartPlayer> missingPlayers) {
        final String photoUrl_prefix = "https://media.api-sports.io/football/players/";
        final String photoUrl_suffix = ".png";
        List<Player> players = missingPlayers.stream()
                .map(playerResponse -> Player.builder()
                        .id(playerResponse.getPlayer().getId())
                        .name(playerResponse.getPlayer().getName())
                        .number(playerResponse.getPlayer().getNumber())
                        .photoUrl(photoUrl_prefix + playerResponse.getPlayer().getId() + photoUrl_suffix)
                        .build())
                .toList();
        playerRepository.saveAll(players);
        log.info("캐싱되지 않은 선수들을 라인업 데이터로 저장했습니다. players={}", players);
    }

    /**
     * 선발 라인업 정보를 이용해 MatchPlayer Entity 를 생성하고 저장합니다. <br>
     * id 가 null 인 경우 Player 연관관계를 맺지 않고 MatchPlayer 의 unregisteredPlayer 필드를 사용합니다.
     * @param lineups
     * @param matchLineup
     * @param isSubstitute
     * @return
     */
    private List<MatchPlayer> buildAndSaveMatchPlayerEntity(_Lineups lineups, MatchLineup matchLineup, boolean isSubstitute) {
        List<MatchPlayer> matchPlayerList = new ArrayList<>();

        List<_Lineups._StartPlayer> startPlayers = (isSubstitute ? lineups.getSubstitutes() : lineups.getStartXI());

        Map<Long, _Lineups._Player> playerResponseMap = new HashMap<>();
        List<_Lineups._Player> idNullPlayerList = new ArrayList<>();
        startPlayers.forEach(startPlayer -> {
            _Lineups._Player player = startPlayer.getPlayer();
            if (player.getId() == null) {
                idNullPlayerList.add(player);
            } else {
                playerResponseMap.put(player.getId(), player);
            }
        });

        // id 존재하는 선수들은 playerRepository 에서 찾아서 저장
        List<Player> findPlayers = playerRepository.findAllById(playerResponseMap.keySet());
        findPlayers.forEach(player -> {
            _Lineups._Player playerResponse = playerResponseMap.get(player.getId());
            MatchPlayer matchPlayer = MatchPlayer.builder()
                    .matchLineup(matchLineup)
                    .player(player)
                    .position(playerResponse.getPos())
                    .grid(playerResponse.getGrid())
                    .substitute(isSubstitute)
                    .build();
            matchPlayerList.add(matchPlayer);
        });

        // id 가 null 인 선수들은 playerRepository 에 저장되지 않고 MatchPlayer 에만 저장
        idNullPlayerList.forEach(player -> {
            MatchPlayer matchPlayer = MatchPlayer.builder()
                    .matchLineup(matchLineup)
                    .unregisteredPlayerName(player.getName())
                    .unregisteredPlayerNumber(player.getNumber())
                    .position(player.getPos())
                    .grid(player.getGrid())
                    .substitute(isSubstitute)
                    .build();
            matchPlayerList.add(matchPlayer);
        });

        return matchPlayerRepository.saveAll(matchPlayerList);
    }

    /**
     * 이전에 해당 fixture 에 이미 Lineup 이 저장되어있다면, MatchLineup 과 MatchPlayer 가 중복으로 저장될 수 있습니다.
     * 이미 fixture 의 Start Lineup and Player 가 저장되어 있다면, 기존에 저장된 선발 관련 정보를 삭제합니다.
     * @param fixtureId 경기 ID
     */
    public void cleanupPreviousLineup(long fixtureId) {
        log.info("previous start lineup clean up for fixtureId={}", fixtureId);
        Fixture fixture = fixtureRepository.findById(fixtureId).orElseThrow();
        List<MatchLineup> lineups = matchLineupRepository.findAllByFixture(fixture);
        if (!lineups.isEmpty()) {
            log.info("이미 저장된 lineup 정보가 있어, 기존 데이터를 삭제합니다. 기존에 저장된 MatchLineup count : {}", lineups.size());
            int deletedPlayerCount = matchPlayerRepository.deleteByMatchLineupIn(lineups);
            // Force immediate database delete to prevent foreign key constraint violations
            entityManager.flush();
            log.info("삭제된 player count = {}", deletedPlayerCount);
            matchLineupRepository.deleteAllInBatch(lineups);
            log.info("fixtureId={} 라인업 정보 삭제 완료", fixtureId);
        } else {
            log.info("이전에 저장된 fixtureId={} 라인업 정보가 없어, prevCleanup 할 데이터가 없습니다.", fixtureId);
        }
    }


}
