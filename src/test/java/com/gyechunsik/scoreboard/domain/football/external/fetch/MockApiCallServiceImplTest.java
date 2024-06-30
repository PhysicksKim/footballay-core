package com.gyechunsik.scoreboard.domain.football.external.fetch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyechunsik.scoreboard.domain.football.constant.FixtureId;
import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.constant.TeamId;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@Transactional
@ActiveProfiles("mockapi")
@SpringBootTest
class MockApiCallServiceImplTest {

    @Autowired
    private MockApiCallServiceImpl mockApiCallService;
    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @DisplayName("Mock Api 로 _LeagueResponse 반환")
    @Test
    void success_league() {
        // given
        long epl = LeagueId.EPL;

        // when
        LeagueInfoResponse leagueInfoResponse = mockApiCallService.leagueInfo(epl);

        // then
        assertThat(leagueInfoResponse).isNotNull();
        assertThat(leagueInfoResponse.getResponse().get(0)).isNotNull();

        _LeagueResponse leagueResponse = leagueInfoResponse.getResponse().get(0).getLeague();
        log.info("_LeagueResponse _FixtureSingle : {}", leagueResponse);
    }


    @DisplayName("Mock Api 로 team 의 현재 leagues 를 반환")
    @Test
    void success_teamCurrentLeagues() {
        // given
        long manutd = TeamId.MANUTD;

        // when
        LeagueInfoResponse leagueInfoResponse = mockApiCallService.teamCurrentLeaguesInfo(manutd);

        // then
        assertThat(leagueInfoResponse).isNotNull();
        assertThat(leagueInfoResponse.getResponse()).size().isGreaterThan(2);
        assertThat(leagueInfoResponse.getResponse().get(0)).isNotNull();
        for (LeagueInfoResponse._Response response : leagueInfoResponse.getResponse()) {
            log.info("_LeagueResponse _FixtureSingle : {}", response);
        }
    }

    @DisplayName("Mock Api 로 _Team 반환")
    @Test
    void success_team() {
        // given
        long mancity = TeamId.MANCITY;

        // when
        TeamInfoResponse teamInfoResponse = mockApiCallService.teamInfo(mancity);

        // then
        assertThat(teamInfoResponse).isNotNull();
        assertThat(teamInfoResponse.getResponse().get(0)).isNotNull();

        TeamInfoResponse._TeamResponse team = teamInfoResponse.getResponse().get(0).getTeam();
        log.info("_Team _FixtureSingle : {}", team);
    }

    @DisplayName("LeagueId 와 CurrentSeason 으로 해당 리그의 팀들을 조회한다")
    @Test
    void success_teamsInfoByLeagueIdAndCurrentSeason() {
        // given
        final long leagueId = LeagueId.EPL; // 39
        final int currentSeason = 2023; // 2023

        // when
        TeamInfoResponse teamInfoResponse = mockApiCallService.teamsInfo(leagueId, currentSeason);

        // then
        assertThat(teamInfoResponse).isNotNull();
        assertThat(teamInfoResponse.getResponse().get(0)).isNotNull();
        log.info("teamInfo :: {}", teamInfoResponse);
    }

    @DisplayName("Mock Api 로 player Squad 반환")
    @Test
    void success_playerSquad() {
        // given
        long mancity = TeamId.MANCITY;

        // when
        PlayerSquadResponse playerSquadResponse = mockApiCallService.playerSquad(mancity);

        // then
        assertThat(playerSquadResponse).isNotNull();
        assertThat(playerSquadResponse.getResponse().get(0)).isNotNull();

        List<PlayerSquadResponse._PlayerData> players = playerSquadResponse.getResponse().get(0).getPlayers();
        for (PlayerSquadResponse._PlayerData player : players) {
            log.info("player : {}", player);
        }
    }

    @DisplayName("단일 fixture 조회")
    @Test
    void success_fixtureSingle() throws JsonProcessingException {
        // given
        long fixtureId = FixtureId.FIXTURE_SINGLE_1145526;

        // when
        FixtureSingleResponse fixture = mockApiCallService.fixtureSingle(fixtureId);
        String json = jacksonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(fixture);
        log.info("FixtureSingleResponse : {}", json);
    }
}