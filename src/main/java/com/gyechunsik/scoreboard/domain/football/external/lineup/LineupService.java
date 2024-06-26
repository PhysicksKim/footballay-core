package com.gyechunsik.scoreboard.domain.football.external.lineup;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.Player;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.entity.live.StartLineup;
import com.gyechunsik.scoreboard.domain.football.entity.live.StartPlayer;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.TeamRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.StartLineupRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.StartPlayerRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse.*;

@RequiredArgsConstructor
@Transactional
@Service
public class LineupService {

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
        List<FixtureSingle> fixtureSingleResponse = response.getResponse();
        if (fixtureSingleResponse.isEmpty()) {
            throw new IllegalArgumentException("Lineup 정보가 없습니다.");
        }

        // 라인업을 위한 정보 추출
        FixtureSingle fixtureSingle = fixtureSingleResponse.get(0);
        // Fixture ID 추출
        Long fixtureIdResponse = fixtureSingle.getFixture().getId();
        // 리그 추출
        FixtureSingleResponse.League leagueResponse = fixtureSingle.getLeague();
        // 팀 추출
        Home homeResponse = fixtureSingle.getTeams().getHome();
        Away awayResponse = fixtureSingle.getTeams().getAway();
        List<Lineups> lineups = fixtureSingle.getLineups();
        // 라인업 추출
        Lineups homeLineupResponse = lineups.stream()
                .filter(lineup -> lineup.getTeam().getId().equals(homeResponse.getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("홈팀 라인업 정보가 없습니다."));
        Lineups awayLineupResponse = lineups.stream()
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
    }

    private List<StartPlayer> buildAndSaveStartPlayerEntity(Lineups lineups, StartLineup homeStartLineup, boolean isSubstitute) {
        Map<Long, Lineups.StartXI.Player> playerResponseMap = lineups.getStartXI().stream()
                .collect(Collectors.toMap(player -> player.getPlayer().getId(), player -> player.getPlayer()));
        List<Player> findPlayers = playerRepository.findAllById(playerResponseMap.keySet());
        List<StartPlayer> startPlayerList = new ArrayList<>();
        findPlayers.forEach(player -> {
            Lineups.StartXI.Player playerResponse = playerResponseMap.get(player.getId());
            StartPlayer startPlayer = StartPlayer.builder()
                    .startLineup(homeStartLineup)
                    .player(player)
                    .position(playerResponse.getPos())
                    .grid(playerResponse.getGrid())
                    .substitute(isSubstitute)
                    .build();
            startPlayerList.add(startPlayer);
        });
        return startPlayerRepository.saveAll(startPlayerList);
    }

    @Getter
    @AllArgsConstructor
    private static class PlayerResponse {
        long id;
        String position;
        String grid;
        boolean substitute;

        @Setter
        Player playerEntity;

        public PlayerResponse(FixtureSingleResponse.Lineups.StartXI player, boolean substitute) {
            this.id = player.getPlayer().getId();
            this.position = player.getPlayer().getPos();
            this.grid = player.getPlayer().getGrid();
            this.substitute = substitute;
        }

        public PlayerResponse(FixtureSingleResponse.Lineups.Substitute player, boolean substitute) {
            this.id = player.getPlayer().getId();
            this.position = player.getPlayer().getPos();
            this.grid = player.getPlayer().getGrid();
            this.substitute = substitute;
        }
    }

}
