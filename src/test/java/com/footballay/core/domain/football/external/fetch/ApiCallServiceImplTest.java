package com.footballay.core.domain.football.external.fetch;

import com.footballay.core.domain.football.constant.LeagueId;
import com.footballay.core.domain.football.constant.PlayerId;
import com.footballay.core.domain.football.constant.TeamId;
import com.footballay.core.domain.football.external.fetch.response.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.shaded.com.fasterxml.jackson.core.JsonProcessingException;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// @Disabled
@Slf4j
@SpringBootTest
@ActiveProfiles({"api","cookies"})
class ApiCallServiceImplTest {

    @Autowired
    private ApiCallServiceImpl apiCallService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @DisplayName("실제 api ; api 상태를 확인합니다")
    @Test
    void success_apistatus() throws IOException {
        // when
        ExternalApiStatusResponse status = apiCallService.status();

        // then
        log.info("status : {}", objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(status));
    }

    @DisplayName("실제 api ; 리그 id로 리그 정보를 얻어옵니다")
    @Test
    void success_leagueInfo() throws IOException {
        // given
        long epl2324 = LeagueId.EPL;

        // when
        LeagueInfoResponse leagueInfoResponse = apiCallService.leagueInfo(epl2324);

        // then
        log.info("league info raw response : {}", objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(leagueInfoResponse));

        assertThat(leagueInfoResponse.getResponse()).hasSize(1);
        assertThat(leagueInfoResponse.getResponse().get(0)).isNotNull();
    }

    @DisplayName("리그 아이디로 해당 리그의 팀들을 조회")
    @Test
    void success_teamsByLeagueId() {
        // given
        long epl = LeagueId.EPL;
        int currentSeason = 2023;

        // when
        TeamInfoResponse teamInfoResponse = apiCallService.teamsInfo(epl, currentSeason);

        // then
        for (TeamInfoResponse._TeamInfo teamInfo : teamInfoResponse.getResponse()) {
            TeamInfoResponse._TeamResponse team = teamInfo.getTeam();
            log.info("_Team :: {}", team);
        }
    }

    @DisplayName("실제 api ; 팀 id로 팀 정보를 얻어옵니다")
    @Test
    void success_teamInfo() throws IOException {
        // given
        long manutd = TeamId.MANUTD;

        // when
        TeamInfoResponse teamInfoResponse = apiCallService.teamInfo(manutd);

        // then
        log.info("team info raw response : {}", objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(teamInfoResponse));

        assertThat(teamInfoResponse.getResponse()).hasSize(1);
        assertThat(teamInfoResponse.getResponse().get(0)).isNotNull();
    }

    @DisplayName("실제 api ; teamId 로 player squad 정보를 얻어옵니다")
    @Test
    void Success_player_squad() throws IOException {
        // given
        long manutd = TeamId.MANUTD;

        // when
        PlayerSquadResponse playerSquadResponse = apiCallService.playerSquad(manutd);

        // then
        PlayerSquadResponse._PlayerData playerData = playerSquadResponse.getResponse().get(0).getPlayers().get(0);
        List<PlayerSquadResponse._PlayerData> players = playerSquadResponse.getResponse().get(0).getPlayers();
        log.info("response : {}", objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(playerSquadResponse));
        log.info("first player : {}", objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(playerData));

        // assertions
        assertThat(playerSquadResponse).isNotNull();
        assertThat(playerData).isNotNull();
        assertThat(players).isNotNull();
        assertThat(playerSquadResponse.getResponse()).hasSize(1);
        assertThat(playerSquadResponse.getGet()).isEqualTo("players/squads");
    }

    @DisplayName("실제 API : teamId 로 해당 팀이 현재 참여중인 _StandingResponseData 정보들을 가져옵니다")
    @Test
    void success_currentTeamLeaguesInfo() throws JsonProcessingException {
        // given
        long manutd = TeamId.MANUTD;

        // when
        LeagueInfoResponse leagueInfoResponse = apiCallService.teamCurrentLeaguesInfo(manutd);
        log.info("league info raw response : {}", objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(leagueInfoResponse)
        );

        // then
        assertThat(leagueInfoResponse.getResponse()).size().isGreaterThan(1);
        assertThat(leagueInfoResponse.getResponse().get(0)).isNotNull();
    }

    @DisplayName("실제 API : leagueId, season 으로 해당 리그의 일정을 가져옵니다")
    @Test
    void success_fixturesOfLeagueSeason() throws JsonProcessingException {
        // given
        long euro = LeagueId.EURO;
        int season = 2024;

        // when
        FixtureResponse fixtureResponse = apiCallService.fixturesOfLeagueSeason(euro, season);
        log.info("fixture response : {}", objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(fixtureResponse)
        );

        // then
        assertThat(fixtureResponse).isNotNull();
    }

    @DisplayName("실제 API : 선수 한 명의 정보를 가져오기 위해 playerId, leagueId, season 으로 선수 정보를 가져옵니다")
    @Test
    void success_playerSingle() throws JsonProcessingException {
        // given
        // id: "629"
        // league: "39"
        // season: "2024"
        long player = PlayerId.De_Bruyne;
        long league = LeagueId.EPL;
        int season = 2024;

        // when
        PlayerInfoResponse playerSingle = apiCallService.playerSingle(player, league, season);
        log.info("player single response : {}", objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(playerSingle)
        );

        // then
        assertThat(playerSingle).isNotNull();
    }

    @DisplayName("실제 API : 리그 id, 시즌으로 리그 순위 정보를 가져옵니다")
    @Test
    void success_standings() throws JsonProcessingException {
        // given
        long epl = LeagueId.EPL;
        int season = 2024;

        // when
        StandingsResponse standings = apiCallService.standings(epl, season);
        log.info("standing response : {}", objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(standings)
        );

        // then
        assertThat(standings).isNotNull();
    }
}