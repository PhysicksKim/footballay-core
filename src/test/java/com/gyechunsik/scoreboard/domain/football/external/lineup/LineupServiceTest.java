package com.gyechunsik.scoreboard.domain.football.external.lineup;

import com.gyechunsik.scoreboard.domain.football.constant.FixtureId;
import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.persistence.League;
import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.persistence.Team;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchLineup;
import com.gyechunsik.scoreboard.domain.football.persistence.live.MatchPlayer;
import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.FixtureSingleResponse;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.PlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.MatchLineupRepository;
import com.gyechunsik.scoreboard.domain.football.repository.live.MatchPlayerRepository;
import com.gyechunsik.scoreboard.domain.football.repository.relations.TeamPlayerRepository;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@Slf4j
@ActiveProfiles({"dev", "mockapi"})
@SpringBootTest
class LineupServiceTest {

    @Autowired
    LineupService lineupService;

    @Autowired
    private ApiCallService apiCallService;
    @Autowired
    private FootballApiCacheService footballApiCacheService;
    @Autowired
    private FixtureRepository fixtureRepository;
    @Autowired
    private MatchLineupRepository matchLineupRepository;
    @Autowired
    private MatchPlayerRepository matchPlayerRepository;
    @Autowired
    private PlayerRepository playerRepository;

    @Autowired
    private EntityManager em;
    @Autowired
    private TeamPlayerRepository teamPlayerRepository;

    @Transactional
    @DisplayName("singleFixture 응답으로 MatchLineup 과 MatchPlayer 저장 성공")
    @Test
    void singleFixture() {
        // given
        League league = footballApiCacheService.cacheLeague(LeagueId.EURO);
        List<Team> teams = footballApiCacheService.cacheTeamsOfLeague(league.getLeagueId());
        for (Team team : teams) {
            footballApiCacheService.cacheTeamSquad(team.getId());
        }
        List<Fixture> fixtures = footballApiCacheService.cacheFixturesOfLeague(league.getLeagueId());

        // when
        log.info("BEFORE fixture single response API request");
        FixtureSingleResponse fixtureSingleResponse = apiCallService.fixtureSingle(FixtureId.FIXTURE_SINGLE_1145526);

        lineupService.saveLineup(fixtureSingleResponse);

        // then
        fixtureRepository.findById(FixtureId.FIXTURE_SINGLE_1145526).ifPresent(fixture -> {
            log.info("fixture :: {}", fixture);
        });
        List<MatchLineup> all = matchLineupRepository.findAll();
        Fixture fixture = fixtureRepository.findById(FixtureId.FIXTURE_SINGLE_1145526).orElseThrow();
        MatchLineup homeMatchLineup = matchLineupRepository.findByFixtureAndTeam(fixture, fixture.getHomeTeam()).orElseThrow();
        MatchLineup awayMatchLineup = matchLineupRepository.findByFixtureAndTeam(fixture, fixture.getAwayTeam()).orElseThrow();
        log.info("homeMatchLineup :: {}", homeMatchLineup);

        List<MatchPlayer> homeLineupPlayers = matchPlayerRepository.findByMatchLineup(homeMatchLineup);
        homeLineupPlayers.forEach(startPlayer -> {
            log.info("startPlayer :: (id={},name={},isSub={})",
                    startPlayer.getPlayer().getId(),
                    startPlayer.getPlayer().getName(),
                    startPlayer.getSubstitute()
            );
        });
        List<MatchPlayer> awayLineupPlayers = matchPlayerRepository.findByMatchLineup(awayMatchLineup);
        awayLineupPlayers.forEach(startPlayer -> {
            log.info("startPlayer :: (id={},name={},isSub={})",
                    startPlayer.getPlayer().getId(),
                    startPlayer.getPlayer().getName(),
                    startPlayer.getSubstitute()
            );
        });

        // assert
        assertThat(all.size()).isEqualTo(2);
        assertThat(homeLineupPlayers).size().isGreaterThan(11);
        assertThat(awayLineupPlayers).size().isGreaterThan(11);
    }

    @Transactional
    @DisplayName("라인업 데이터가 없는 경우 isNeedToCleanUpAndReSaveLineup 테스트")
    @Test
    void testIsNeedToCleanUpAndReSaveLineup_NoLineupData() {
        // given
        League league = footballApiCacheService.cacheLeague(LeagueId.EURO);
        List<Team> teams = footballApiCacheService.cacheTeamsOfLeague(league.getLeagueId());
        for (Team team : teams) {
            footballApiCacheService.cacheTeamSquad(team.getId());
        }
        footballApiCacheService.cacheFixturesOfLeague(league.getLeagueId());

        // Fetch the fixture single response
        FixtureSingleResponse response = apiCallService.fixtureSingle(FixtureId.FIXTURE_SINGLE_1145526);

        // Modify the response to have no lineup data
        response.getResponse().get(0).setLineups(new ArrayList<>());

        // when
        boolean needResave = lineupService.isNeedToCleanUpAndReSaveLineup(response);

        // then
        assertThat(needResave).isFalse();
    }

    @Transactional
    @DisplayName("미등록 선수가 있는 경우 saveLineup 테스트")
    @Test
    void testSaveLineup_WithUnregisteredPlayers() {
        // given
        League league = footballApiCacheService.cacheLeague(LeagueId.EURO);
        List<Team> teams = footballApiCacheService.cacheTeamsOfLeague(league.getLeagueId());
        for (Team team : teams) {
            footballApiCacheService.cacheTeamSquad(team.getId());
        }
        footballApiCacheService.cacheFixturesOfLeague(league.getLeagueId());

        FixtureSingleResponse response = apiCallService.fixtureSingle(FixtureId.FIXTURE_SINGLE_1145526);

        // Modify the response to set some player IDs to null to simulate unregistered players
        response.getResponse().get(0).getLineups().forEach(lineup -> {
            if (!lineup.getStartXI().isEmpty()) {
                lineup.getStartXI().get(0).getPlayer().setId(null);
                if (lineup.getStartXI().size() > 1) {
                    lineup.getStartXI().get(1).getPlayer().setId(null);
                }
            }
            if (!lineup.getSubstitutes().isEmpty()) {
                lineup.getSubstitutes().get(0).getPlayer().setId(null);
                if (lineup.getSubstitutes().size() > 1) {
                    lineup.getSubstitutes().get(1).getPlayer().setId(null);
                }
            }
        });

        // when
        boolean result = lineupService.saveLineup(response);

        // then
        assertThat(result).isFalse(); // Since there are unregistered players

        // Verify that MatchLineup and MatchPlayer have been saved
        Fixture fixture = fixtureRepository.findById(FixtureId.FIXTURE_SINGLE_1145526).orElseThrow();
        List<MatchLineup> matchLineups = matchLineupRepository.findAllByFixture(fixture);
        assertThat(matchLineups).hasSize(2);

        // Verify that unregistered players have been handled correctly
        matchLineups.forEach(matchLineup -> {
            List<MatchPlayer> matchPlayers = matchPlayerRepository.findByMatchLineup(matchLineup);
            assertThat(matchPlayers).isNotEmpty();
            boolean hasUnregisteredPlayer = matchPlayers.stream()
                    .anyMatch(mp -> mp.getPlayer() == null && mp.getUnregisteredPlayerName() != null);
            assertThat(hasUnregisteredPlayer).isTrue();
        });
    }

    @Transactional
    @DisplayName("선수 수가 불일치하는 경우 isNeedToCleanUpAndReSaveLineup 테스트")
    @Test
    void testIsNeedToCleanUpAndReSaveLineup_PlayerCountMismatch() {
        // given
        League league = footballApiCacheService.cacheLeague(LeagueId.EURO);
        List<Team> teams = footballApiCacheService.cacheTeamsOfLeague(league.getLeagueId());
        for (Team team : teams) {
            footballApiCacheService.cacheTeamSquad(team.getId());
        }
        footballApiCacheService.cacheFixturesOfLeague(league.getLeagueId());

        // Initial lineup save
        FixtureSingleResponse initialResponse = apiCallService.fixtureSingle(FixtureId.FIXTURE_SINGLE_1145526);
        lineupService.saveLineup(initialResponse);

        // Modify the response to remove a player
        FixtureSingleResponse modifiedResponse = apiCallService.fixtureSingle(FixtureId.FIXTURE_SINGLE_1145526);
        modifiedResponse.getResponse().get(0).getLineups().forEach(lineup -> {
            if (!lineup.getStartXI().isEmpty()) {
                lineup.getStartXI().remove(0);
            }
        });

        // when
        boolean needResave = lineupService.isNeedToCleanUpAndReSaveLineup(modifiedResponse);

        // then
        assertThat(needResave).isTrue();
    }

    @Transactional
    @DisplayName("이미 라인업 데이터가 존재하는 경우 saveLineup 테스트")
    @Test
    void testSaveLineup_AlreadyExists() {
        // given
        League league = footballApiCacheService.cacheLeague(LeagueId.EURO);
        List<Team> teams = footballApiCacheService.cacheTeamsOfLeague(league.getLeagueId());
        for (Team team : teams) {
            footballApiCacheService.cacheTeamSquad(team.getId());
        }
        footballApiCacheService.cacheFixturesOfLeague(league.getLeagueId());

        FixtureSingleResponse response = apiCallService.fixtureSingle(FixtureId.FIXTURE_SINGLE_1145526);
        lineupService.saveLineup(response);

        // when
        assertThrows(IllegalStateException.class, () -> lineupService.saveLineup(response));
    }

    @Transactional
    @DisplayName("선수의 번호가 변경된 경우 saveLineup 테스트")
    @Test
    void testSaveLineup_PlayerNumberUpdate() {
        // given
        League league = footballApiCacheService.cacheLeague(LeagueId.EURO);
        List<Team> teams = footballApiCacheService.cacheTeamsOfLeague(league.getLeagueId());
        for (Team team : teams) {
            footballApiCacheService.cacheTeamSquad(team.getId());
        }
        footballApiCacheService.cacheFixturesOfLeague(league.getLeagueId());

        FixtureSingleResponse response = apiCallService.fixtureSingle(FixtureId.FIXTURE_SINGLE_1145526);
        lineupService.saveLineup(response);

        em.flush();
        em.clear();

        // Modify the player numbers
        response.getResponse().get(0).getLineups().forEach(lineup -> {
            lineup.getStartXI().forEach(player -> {
                if (player.getPlayer().getNumber() != null) {
                    player.getPlayer().setNumber(player.getPlayer().getNumber() + 10); // Increase by 10
                }
            });
            lineup.getSubstitutes().forEach(player -> {
                if (player.getPlayer().getNumber() != null) {
                    player.getPlayer().setNumber(player.getPlayer().getNumber() + 10); // Increase by 10
                }
            });
        });

        // when
        lineupService.cacheAndUpdateFromLineupPlayers(response.getResponse().get(0).getLineups().get(0));
        lineupService.cacheAndUpdateFromLineupPlayers(response.getResponse().get(0).getLineups().get(1));

        // then
        response.getResponse().get(0).getLineups().forEach(lineup -> {
            lineup.getStartXI().forEach(playerResponse -> {
                Long playerId = playerResponse.getPlayer().getId();
                Integer expectedNumber = playerResponse.getPlayer().getNumber();
                if (playerId != null) {
                    playerRepository.findById(playerId).ifPresent(playerEntity -> {
                        assertThat(playerEntity.getNumber()).isEqualTo(expectedNumber);
                    });
                }
            });
            lineup.getSubstitutes().forEach(playerResponse -> {
                Long playerId = playerResponse.getPlayer().getId();
                Integer expectedNumber = playerResponse.getPlayer().getNumber();
                if (playerId != null) {
                    playerRepository.findById(playerId).ifPresent(playerEntity -> {
                        assertThat(playerEntity.getNumber()).isEqualTo(expectedNumber);
                    });
                }
            });
        });
    }

    @Transactional
    @DisplayName("라인업 데이터가 없을 때 saveLineup 호출 테스트")
    @Test
    void testSaveLineup_NoLineupData() {
        // given
        League league = footballApiCacheService.cacheLeague(LeagueId.EURO);
        List<Team> teams = footballApiCacheService.cacheTeamsOfLeague(league.getLeagueId());
        for (Team team : teams) {
            footballApiCacheService.cacheTeamSquad(team.getId());
        }
        footballApiCacheService.cacheFixturesOfLeague(league.getLeagueId());

        FixtureSingleResponse response = apiCallService.fixtureSingle(FixtureId.FIXTURE_SINGLE_1145526);

        // Remove the lineup data
        response.getResponse().get(0).setLineups(new ArrayList<>());

        // when
        boolean result = lineupService.saveLineup(response);

        // then
        assertThat(result).isFalse(); // Since there's no lineup data
    }

    @Transactional
    @DisplayName("등록된 선수의 수가 일치하는 경우 isNeedToCleanUpAndReSaveLineup 테스트")
    @Test
    void testIsNeedToCleanUpAndReSaveLineup_NoMismatch() {
        // given
        League league = footballApiCacheService.cacheLeague(LeagueId.EURO);
        List<Team> teams = footballApiCacheService.cacheTeamsOfLeague(league.getLeagueId());
        for (Team team : teams) {
            footballApiCacheService.cacheTeamSquad(team.getId());
        }
        footballApiCacheService.cacheFixturesOfLeague(league.getLeagueId());

        // Initial lineup save
        FixtureSingleResponse initialResponse = apiCallService.fixtureSingle(FixtureId.FIXTURE_SINGLE_1145526);
        lineupService.saveLineup(initialResponse);

        em.flush();
        em.clear();

        // Fetch the same response
        FixtureSingleResponse sameResponse = apiCallService.fixtureSingle(FixtureId.FIXTURE_SINGLE_1145526);

        // when
        boolean needResave = lineupService.isNeedToCleanUpAndReSaveLineup(sameResponse);

        // then
        assertThat(needResave).isFalse();
    }

    @Transactional
    @DisplayName("캐싱되지 않은 선수가 있는 경우 saveLineup 테스트")
    @Test
    void testSaveLineup_WithMissingPlayers() {
        // given
        League league = footballApiCacheService.cacheLeague(LeagueId.EURO);
        List<Team> teams = footballApiCacheService.cacheTeamsOfLeague(league.getLeagueId());
        for (Team team : teams) {
            footballApiCacheService.cacheTeamSquad(team.getId());
        }
        footballApiCacheService.cacheFixturesOfLeague(league.getLeagueId());


        // Fetch the fixture single response
        FixtureSingleResponse response = apiCallService.fixtureSingle(FixtureId.FIXTURE_SINGLE_1145526);

        // Delete some players from the database to simulate missing players
        List<Long> playerIdsToRemove = response.getResponse().get(0).getLineups().stream()
                .flatMap(lineup -> lineup.getStartXI().stream())
                .map(player -> player.getPlayer().getId())
                .limit(2) // Remove two players
                .collect(Collectors.toList());

        playerIdsToRemove.forEach(playerId -> {
            Optional<Player> byId = playerRepository.findById(playerId);
            if(byId.isPresent()) {
                teamPlayerRepository.deleteAll(teamPlayerRepository.findTeamsByPlayer(byId.get()));
                playerRepository.deleteById(playerId);
            }
        });

        // when
        boolean result = lineupService.saveLineup(response);

        // then
        assertThat(result).isTrue();

        // Verify that the missing players have been re-cached
        playerIdsToRemove.forEach(playerId -> {
            assertThat(playerRepository.findById(playerId)).isPresent();
        });
    }
}
