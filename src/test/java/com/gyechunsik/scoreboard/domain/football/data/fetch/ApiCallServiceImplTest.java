package com.gyechunsik.scoreboard.domain.football.data.fetch;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.constant.TeamId;
import com.gyechunsik.scoreboard.domain.football.data.fetch.ApiCallServiceImpl;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.LeagueInfoResponse;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.PlayerSquadResponse;
import com.gyechunsik.scoreboard.domain.football.data.fetch.ApiCallService;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.TeamInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.*;


@Slf4j
@SpringBootTest
@ActiveProfiles("api")
class ApiCallServiceImplTest {

    // @Autowired
    // private ApiCallService apiCallService;

    @Autowired
    private ApiCallServiceImpl apiCallService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DisplayName("리그 id로 리그 정보를 얻어옵니다")
    @Test
    void success_leagueInfo() throws IOException {
        // given
        long epl2324 = LeagueId.EPL;

        // when
        LeagueInfoResponse leagueInfoResponse = apiCallService.leagueInfo(epl2324);

        // then
        log.info("league info raw response : {}", leagueInfoResponse);

        assertThat(leagueInfoResponse.getResponse()).hasSize(1);
        assertThat(leagueInfoResponse.getResponse().get(0)).isNotNull();
    }

    @DisplayName("팀 id로 팀 정보를 얻어옵니다")
    @Test
    void success_teamInfo() throws IOException {
        // given
        long manutd = TeamId.MANUTD;

        // when
        TeamInfoResponse teamInfoResponse = apiCallService.teamInfo(manutd);

        // then
        log.info("team info raw response : {}", teamInfoResponse.toString());

        assertThat(teamInfoResponse.getResponse()).hasSize(1);
        assertThat(teamInfoResponse.getResponse().get(0)).isNotNull();
    }

    @DisplayName("실제 API 의 player/squad 에서 정상 응답 확인")
    @Test
    void Success_player_squad() throws IOException {
        // given
        long manutd = TeamId.MANUTD;

        // when
        PlayerSquadResponse playerSquadResponse = apiCallService.playerSquad(manutd);

        // then
        PlayerSquadResponse.PlayerData playerData = playerSquadResponse.getResponse().get(0).getPlayers().get(0);
        List<PlayerSquadResponse.PlayerData> players = playerSquadResponse.getResponse().get(0).getPlayers();
        log.info("response : {}", playerSquadResponse);
        log.info("first player : {}", playerData);

        // assertions
        assertThat(playerSquadResponse).isNotNull();
        assertThat(playerData).isNotNull();
        assertThat(players).isNotNull();
        assertThat(playerSquadResponse.getResponse()).hasSize(1);
        assertThat(playerSquadResponse.getGet()).isEqualTo("players/squads");
    }
}