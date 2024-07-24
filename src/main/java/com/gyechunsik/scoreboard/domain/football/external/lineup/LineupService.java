package com.gyechunsik.scoreboard.domain.football.external.lineup;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.Player;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.entity.live.StartLineup;
import com.gyechunsik.scoreboard.domain.football.entity.live.StartPlayer;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.StartLineupRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.StartPlayerRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse.*;

@RequiredArgsConstructor
@Transactional
@Service
public class LineupService {

    private static final Logger log = LoggerFactory.getLogger(LineupService.class);
    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final FixtureRepository fixtureRepository;
    private final StartLineupRepository startLineupRepository;
    private final StartPlayerRepository startPlayerRepository;

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

        // 1. StartLineup Entity 생성
        Fixture fixture = fixtureRepository.findById(fixtureIdResponse)
                .orElseThrow(() -> new IllegalArgumentException("경기 정보가 아직 캐싱되지 않았습니다."));
        Team homeTeam = teamRepository.findById(homeResponse.getId())
                .orElseThrow(() -> new IllegalArgumentException("홈팀이 아직 캐싱되지 않았습니다."));
        Team awayTeam = teamRepository.findById(awayResponse.getId())
                .orElseThrow(() -> new IllegalArgumentException("어웨이팀이 아직 캐싱되지 않았습니다."));
        StartLineup homeLineup = StartLineup.builder()
                .fixture(fixture)
                .formation(homeLineupResponse.getFormation())
                .team(homeTeam)
                .build();
        StartLineup awayLineup = StartLineup.builder()
                .fixture(fixture)
                .formation(awayLineupResponse.getFormation())
                .team(awayTeam)
                .build();
        StartLineup homeStartLineup = startLineupRepository.save(homeLineup);
        StartLineup awayStartLineup = startLineupRepository.save(awayLineup);

        // 2. StartPlayer Entity 생성
        List<StartPlayer> homeStartPlayerList = buildAndSaveStartPlayerEntity(homeLineupResponse, homeStartLineup, false);
        List<StartPlayer> homeSubstitutePlayerList = buildAndSaveStartPlayerEntity(homeLineupResponse, homeStartLineup, true);
        List<StartPlayer> awayStartPlayerList = buildAndSaveStartPlayerEntity(awayLineupResponse, awayStartLineup, false);
        List<StartPlayer> awaySubstitutePlayerList = buildAndSaveStartPlayerEntity(awayLineupResponse, awayStartLineup, true);
        log.info("fixtureId={} 라인업 정보 저장 완료", fixtureIdResponse);
        log.info("홈팀 라인업 정보 저장 완료. fixtureId={}, teamId={}, startXI.size={}, subs.size={}",
                fixtureIdResponse, homeTeam.getId(), homeStartPlayerList.size(), homeSubstitutePlayerList.size());
        log.info("어웨이팀 라인업 정보 저장 완료. fixtureId={}, teamId={}, startXI.size={}, subs.size={}",
                fixtureIdResponse, awayTeam.getId(), awayStartPlayerList.size(), awaySubstitutePlayerList.size());
    }

    private List<StartPlayer> buildAndSaveStartPlayerEntity(_Lineups lineups, StartLineup startLineup, boolean isSubstitute) {
        List<StartPlayer> startPlayerList = new ArrayList<>();

        List<_Lineups._StartPlayer> startPlayers = (isSubstitute ? lineups.getSubstitutes() : lineups.getStartXI());
        Map<Long, _Lineups._Player> playerResponseMap = startPlayers.stream()
                .collect(Collectors.toMap(player -> player.getPlayer().getId(), player -> player.getPlayer()));
        List<Player> findPlayers = playerRepository.findAllById(playerResponseMap.keySet());

        findPlayers.forEach(player -> {
            _Lineups._Player playerResponse = playerResponseMap.get(player.getId());
            StartPlayer startPlayer = StartPlayer.builder()
                    .startLineup(startLineup)
                    .player(player)
                    .position(playerResponse.getPos())
                    .grid(playerResponse.getGrid())
                    .substitute(isSubstitute)
                    .build();
            startPlayerList.add(startPlayer);
        });
        return startPlayerRepository.saveAll(startPlayerList);
    }

    /**
     * 이전에 해당 fixture 에 이미 Lineup 이 저장되어있다면, StartLineup 과 StartPlayer 가 중복으로 저장될 수 있습니다.
     * 이미 fixture 의 Start Lineup and Player 가 저장되어 있다면, 기존에 저장된 선발 관련 정보를 삭제합니다.
     * @param fixtureId 경기 ID
     */
    public void cleanupPreviousLineup(long fixtureId) {
        log.info("previous start lineup clean up for fixtureId={}", fixtureId);
        Fixture fixture = fixtureRepository.findById(fixtureId).orElseThrow();
        List<StartLineup> lineups = startLineupRepository.findAllByFixture(fixture);
        if(!lineups.isEmpty()) {
            log.info("이미 저장된 lineup 정보가 있어, 기존 데이터를 삭제합니다. 기존에 저장된 StartLineup count : {}", lineups.size());
            int deletedPlayerCount = startPlayerRepository.deleteByStartLineupIn(lineups);
            startLineupRepository.deleteAllInBatch(lineups);
            log.info("fixtureId={} 라인업 정보 삭제 완료. 삭제된 player count = {}", fixtureId,deletedPlayerCount);
        } else {
            log.info("fixtureId={} 라인업 정보가 없어, prevCleanup 할 데이터가 없습니다.", fixtureId);
        }
    }

}
