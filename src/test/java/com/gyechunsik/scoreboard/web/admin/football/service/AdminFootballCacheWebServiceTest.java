package com.gyechunsik.scoreboard.web.admin.football.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyechunsik.scoreboard.domain.football.FootballRoot;
import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import com.gyechunsik.scoreboard.web.common.service.ApiCommonResponseService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;
@Slf4j
@SpringBootTest
@ActiveProfiles("mockapi")
class AdminFootballCacheWebServiceTest {

    @Mock
    private FootballRoot footballRoot;

    @Autowired
    private ApiCommonResponseService apiCommonResponseService;

    private AdminFootballCacheWebService adminFootballCacheWebService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this); // Mockito 주입 초기화
        adminFootballCacheWebService = new AdminFootballCacheWebService(footballRoot, apiCommonResponseService);
    }

    @DisplayName("리그 캐싱 성공")
    @Test
    void 리그캐싱성공() throws JsonProcessingException {
        // given
        final Long leagueId = LeagueId.EURO;
        when(footballRoot.cacheLeagueById(leagueId)).thenReturn(true);

        // when
        ApiResponse<Void> test = adminFootballCacheWebService.cacheLeague(leagueId, "test");
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(test);
        log.info(json);

        // then
        verify(footballRoot, times(1)).cacheLeagueById(leagueId);
        assertThat(test).isNotNull();
        assertThat(test.metaData()).isNotNull();
        assertThat(test.metaData().responseCode())
                .isEqualTo(ApiCommonResponseService.CODE_SUCCESS);
        assertThat(test.metaData().status())
                .isEqualTo(ApiCommonResponseService.STATUS_SUCCESS);
    }

    @DisplayName("리그 캐싱 실패")
    @Test
    void 리그캐싱실패() throws JsonProcessingException {
        // given
        final Long leagueId = LeagueId.EURO;
        when(footballRoot.cacheLeagueById(leagueId)).thenReturn(false);

        // when
        ApiResponse<Void> test = adminFootballCacheWebService.cacheLeague(leagueId, "test");
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(test);
        log.info(json);

        // then
        verify(footballRoot, times(1)).cacheLeagueById(leagueId);
        assertThat(test).isNotNull();
        assertThat(test.metaData()).isNotNull();
        assertThat(test.metaData().responseCode())
                .isEqualTo(ApiCommonResponseService.CODE_FAILURE);
        assertThat(test.metaData().status())
                .isEqualTo(ApiCommonResponseService.STATUS_FAILURE);
    }

    @DisplayName("모든 현재 리그 캐싱 성공")
    @Test
    void 모든현재리그캐싱성공() throws JsonProcessingException {
        // given
        when(footballRoot.cacheAllCurrentLeagues()).thenReturn(true);

        // when
        ApiResponse<Void> test = adminFootballCacheWebService.cacheAllCurrentLeagues("test");
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(test);
        log.info(json);

        // then
        verify(footballRoot, times(1)).cacheAllCurrentLeagues();
        assertThat(test).isNotNull();
        assertThat(test.metaData()).isNotNull();
        assertThat(test.metaData().responseCode())
                .isEqualTo(ApiCommonResponseService.CODE_SUCCESS);
        assertThat(test.metaData().status())
                .isEqualTo(ApiCommonResponseService.STATUS_SUCCESS);
    }

    @DisplayName("모든 현재 리그 캐싱 실패")
    @Test
    void 모든현재리그캐싱실패() throws JsonProcessingException {
        // given
        when(footballRoot.cacheAllCurrentLeagues()).thenReturn(false);

        // when
        ApiResponse<Void> test = adminFootballCacheWebService.cacheAllCurrentLeagues("test");
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(test);
        log.info(json);

        // then
        verify(footballRoot, times(1)).cacheAllCurrentLeagues();
        assertThat(test).isNotNull();
        assertThat(test.metaData()).isNotNull();
        assertThat(test.metaData().responseCode())
                .isEqualTo(ApiCommonResponseService.CODE_FAILURE);
        assertThat(test.metaData().status())
                .isEqualTo(ApiCommonResponseService.STATUS_FAILURE);
    }

    @DisplayName("팀 및 현재 리그 캐싱 성공")
    @Test
    void 팀및현재리그캐싱성공() throws JsonProcessingException {
        // given
        final Long teamId = 1L;
        when(footballRoot.cacheTeamAndCurrentLeagues(teamId)).thenReturn(true);

        // when
        ApiResponse<Void> test = adminFootballCacheWebService.cacheTeamAndCurrentLeagues(teamId, "test");
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(test);
        log.info(json);

        // then
        verify(footballRoot, times(1)).cacheTeamAndCurrentLeagues(teamId);
        assertThat(test).isNotNull();
        assertThat(test.metaData()).isNotNull();
        assertThat(test.metaData().responseCode())
                .isEqualTo(ApiCommonResponseService.CODE_SUCCESS);
        assertThat(test.metaData().status())
                .isEqualTo(ApiCommonResponseService.STATUS_SUCCESS);
    }

    @DisplayName("팀 및 현재 리그 캐싱 실패")
    @Test
    void 팀및현재리그캐싱실패() throws JsonProcessingException {
        // given
        final Long teamId = 1L;
        when(footballRoot.cacheTeamAndCurrentLeagues(teamId)).thenReturn(false);

        // when
        ApiResponse<Void> test = adminFootballCacheWebService.cacheTeamAndCurrentLeagues(teamId, "test");
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(test);
        log.info(json);

        // then
        verify(footballRoot, times(1)).cacheTeamAndCurrentLeagues(teamId);
        assertThat(test).isNotNull();
        assertThat(test.metaData()).isNotNull();
        assertThat(test.metaData().responseCode())
                .isEqualTo(ApiCommonResponseService.CODE_FAILURE);
        assertThat(test.metaData().status())
                .isEqualTo(ApiCommonResponseService.STATUS_FAILURE);
    }

    @DisplayName("팀 캐싱 성공")
    @Test
    void 팀캐싱성공() throws JsonProcessingException {
        // given
        final Long teamId = 1L;
        when(footballRoot.cacheTeamAndCurrentLeagues(teamId)).thenReturn(true);

        // when
        ApiResponse<Void> test = adminFootballCacheWebService.cacheTeam(teamId, "test");
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(test);
        log.info(json);

        // then
        verify(footballRoot, times(1)).cacheTeamAndCurrentLeagues(teamId);
        assertThat(test).isNotNull();
        assertThat(test.metaData()).isNotNull();
        assertThat(test.metaData().responseCode())
                .isEqualTo(ApiCommonResponseService.CODE_SUCCESS);
        assertThat(test.metaData().status())
                .isEqualTo(ApiCommonResponseService.STATUS_SUCCESS);
    }

    @DisplayName("팀 캐싱 실패")
    @Test
    void 팀캐싱실패() throws JsonProcessingException {
        // given
        final Long teamId = 1L;
        when(footballRoot.cacheTeamAndCurrentLeagues(teamId)).thenReturn(false);

        // when
        ApiResponse<Void> test = adminFootballCacheWebService.cacheTeam(teamId, "test");
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(test);
        log.info(json);

        // then
        verify(footballRoot, times(1)).cacheTeamAndCurrentLeagues(teamId);
        assertThat(test).isNotNull();
        assertThat(test.metaData()).isNotNull();
        assertThat(test.metaData().responseCode())
                .isEqualTo(ApiCommonResponseService.CODE_FAILURE);
        assertThat(test.metaData().status())
                .isEqualTo(ApiCommonResponseService.STATUS_FAILURE);
    }

    @DisplayName("리그에 속한 팀 캐싱 성공")
    @Test
    void 리그에속한팀캐싱성공() throws JsonProcessingException {
        // given
        final Long leagueId = 1L;
        when(footballRoot.cacheTeamsOfLeague(leagueId)).thenReturn(true);

        // when
        ApiResponse<Void> test = adminFootballCacheWebService.cacheTeamsByLeagueId(leagueId, "test");
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(test);
        log.info(json);

        // then
        verify(footballRoot, times(1)).cacheTeamsOfLeague(leagueId);
        assertThat(test).isNotNull();
        assertThat(test.metaData()).isNotNull();
        assertThat(test.metaData().responseCode())
                .isEqualTo(ApiCommonResponseService.CODE_SUCCESS);
        assertThat(test.metaData().status())
                .isEqualTo(ApiCommonResponseService.STATUS_SUCCESS);
    }

    @DisplayName("리그에 속한 팀 캐싱 실패")
    @Test
    void 리그에속한팀캐싱실패() throws JsonProcessingException {
        // given
        final Long leagueId = 1L;
        when(footballRoot.cacheTeamsOfLeague(leagueId)).thenReturn(false);

        // when
        ApiResponse<Void> test = adminFootballCacheWebService.cacheTeamsByLeagueId(leagueId, "test");
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(test);
        log.info(json);

        // then
        verify(footballRoot, times(1)).cacheTeamsOfLeague(leagueId);
        assertThat(test).isNotNull();
        assertThat(test.metaData()).isNotNull();
        assertThat(test.metaData().responseCode())
                .isEqualTo(ApiCommonResponseService.CODE_FAILURE);
        assertThat(test.metaData().status())
                .isEqualTo(ApiCommonResponseService.STATUS_FAILURE);
    }

    @DisplayName("팀 선수단 캐싱 성공")
    @Test
    void 팀선수단캐싱성공() throws JsonProcessingException {
        // given
        final Long teamId = 1L;
        when(footballRoot.cacheSquadOfTeam(teamId)).thenReturn(true);

        // when
        ApiResponse<Void> test = adminFootballCacheWebService.cacheSquad(teamId, "test");
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(test);
        log.info(json);

        // then
        verify(footballRoot, times(1)).cacheSquadOfTeam(teamId);
        assertThat(test).isNotNull();
        assertThat(test.metaData()).isNotNull();
        assertThat(test.metaData().responseCode())
                .isEqualTo(ApiCommonResponseService.CODE_SUCCESS);
        assertThat(test.metaData().status())
                .isEqualTo(ApiCommonResponseService.STATUS_SUCCESS);
    }

    @DisplayName("팀 선수단 캐싱 실패")
    @Test
    void 팀선수단캐싱실패() throws JsonProcessingException {
        // given
        final Long teamId = 1L;
        when(footballRoot.cacheSquadOfTeam(teamId)).thenReturn(false);

        // when
        ApiResponse<Void> test = adminFootballCacheWebService.cacheSquad(teamId, "test");
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(test);
        log.info(json);

        // then
        verify(footballRoot, times(1)).cacheSquadOfTeam(teamId);
        assertThat(test).isNotNull();
        assertThat(test.metaData()).isNotNull();
        assertThat(test.metaData().responseCode())
                .isEqualTo(ApiCommonResponseService.CODE_FAILURE);
        assertThat(test.metaData().status())
                .isEqualTo(ApiCommonResponseService.STATUS_FAILURE);
    }

    @DisplayName("리그 일정 캐싱 성공")
    @Test
    void 리그일정캐싱성공() throws JsonProcessingException {
        // given
        final Long leagueId = 1L;
        when(footballRoot.cacheAllFixturesOfLeague(leagueId)).thenReturn(true);

        // when
        ApiResponse<Void> test = adminFootballCacheWebService.cacheFixturesOfLeagueCurrentSeason(leagueId, "test");
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(test);
        log.info(json);

        // then
        verify(footballRoot, times(1)).cacheAllFixturesOfLeague(leagueId);
        assertThat(test).isNotNull();
        assertThat(test.metaData()).isNotNull();
        assertThat(test.metaData().responseCode())
                .isEqualTo(ApiCommonResponseService.CODE_SUCCESS);
        assertThat(test.metaData().status())
                .isEqualTo(ApiCommonResponseService.STATUS_SUCCESS);
    }

    @DisplayName("리그 일정 캐싱 실패")
    @Test
    void 리그일정캐싱실패() throws JsonProcessingException {
        // given
        final Long leagueId = 1L;
        when(footballRoot.cacheAllFixturesOfLeague(leagueId)).thenReturn(false);

        // when
        ApiResponse<Void> test = adminFootballCacheWebService.cacheFixturesOfLeagueCurrentSeason(leagueId, "test");
        String json = objectMapper.writerWithDefaultPrettyPrinter()
                .writeValueAsString(test);
        log.info(json);

        // then
        verify(footballRoot, times(1)).cacheAllFixturesOfLeague(leagueId);
        assertThat(test).isNotNull();
        assertThat(test.metaData()).isNotNull();
        assertThat(test.metaData().responseCode())
                .isEqualTo(ApiCommonResponseService.CODE_FAILURE);
        assertThat(test.metaData().status())
                .isEqualTo(ApiCommonResponseService.STATUS_FAILURE);
    }
}
