package com.gyechunsik.scoreboard.web.admin.football.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyechunsik.scoreboard.domain.football.constant.FixtureId;
import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.persistence.League;
import com.gyechunsik.scoreboard.domain.football.scheduler.lineup.StartLineupJobSchedulerService;
import com.gyechunsik.scoreboard.domain.football.scheduler.live.LiveFixtureJobSchedulerService;
import com.gyechunsik.scoreboard.domain.football.service.FootballAvailableService;
import com.gyechunsik.scoreboard.domain.football.util.DevInitData;
import com.gyechunsik.scoreboard.web.admin.football.response.AvailableFixtureDto;
import com.gyechunsik.scoreboard.web.admin.football.response.AvailableLeagueDto;
import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

/**
 * <h3>mockapi profile</h3>
 * mockapi profile 활성화 시, 실제 External Api 대신 MockApiCallService 가 호출됩니다.
 * mockapi 또는 api 중 하나의 프로파일을 꼭 활성화 시켜야 합니다.
 * <h3>dev profile</h3>
 * dev profile 활성화 시, DevInitRunner 가 실행되어 개발용 데이터들이 삽입됩니다.
 */
@Slf4j
@ActiveProfiles({"mockapi","dev"})
@SpringBootTest
@Transactional
class AdminFootballDataWebServiceTest {

    @Autowired
    AdminFootballDataWebService adminFootballDataWebService;

    @Autowired
    private FootballAvailableService footballAvailableService;

    @Autowired
    private DevInitData devInitData;

    @Autowired
    private ObjectMapper jacksonObjectMapper;

    @Autowired
    private Scheduler scheduler;

    @MockBean
    private StartLineupJobSchedulerService startLineupJobSchedulerService;

    @MockBean
    private LiveFixtureJobSchedulerService liveFixtureJobSchedulerService;

    @BeforeEach
    void setup() throws SchedulerException {
        doNothing().when(startLineupJobSchedulerService).addJob(any(Long.class), any(ZonedDateTime.class));
        doNothing().when(liveFixtureJobSchedulerService).addJob(any(Long.class), any(ZonedDateTime.class));
        devInitData.addData();
    }

    @DisplayName("이용가능 리그 명단 조회 성공")
    @Test
    void success_getAvailableLeagues() throws JsonProcessingException {
        // given
        // "dev" profile 의 runner 에 의해서 자동으로 기본 데이터가 삽입됩니다.
        List<League> availableLeagues = footballAvailableService.getAvailableLeagues();
        log.info("availableLeagues={}", availableLeagues);

        // when
        ApiResponse<AvailableLeagueDto> responseAvailableLeagues =
                adminFootballDataWebService.getAvailableLeagues("/api/admin/football/available/leagues/available");

        String responseJsonString = jacksonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(responseAvailableLeagues);
        log.info("responseAvailableLeagues={}", responseJsonString);

        // then
        assertThat(responseAvailableLeagues).isNotNull();
        assertThat(responseAvailableLeagues.metaData().status()).isEqualTo("SUCCESS");
        assertThat(responseAvailableLeagues.metaData().responseCode()).isEqualTo(200);
        assertThat(responseAvailableLeagues.metaData().requestUrl()).isEqualTo("/api/admin/football/available/leagues/available");

        assertThat(responseAvailableLeagues.response()).hasSize(availableLeagues.size());

        for (int i = 0; i < availableLeagues.size(); i++) {
            League expectedLeague = availableLeagues.get(i);
            AvailableLeagueDto actualLeagueDto = responseAvailableLeagues.response()[i];

            assertThat(actualLeagueDto.leagueId()).isEqualTo(expectedLeague.getLeagueId());
            assertThat(actualLeagueDto.name()).isEqualTo(expectedLeague.getName());
            assertThat(actualLeagueDto.koreanName()).isEqualTo(expectedLeague.getKoreanName());
            assertThat(actualLeagueDto.logo()).isEqualTo(expectedLeague.getLogo());
            assertThat(actualLeagueDto.available()).isEqualTo(expectedLeague.isAvailable());
            assertThat(actualLeagueDto.currentSeason()).isEqualTo(expectedLeague.getCurrentSeason());
        }
    }

    @DisplayName("이용가능 리그 추가 성공")
    @Test
    void success_addAvailableLeague() throws JsonProcessingException {
        // given
        long leagueId = LeagueId.EURO; // 예시 리그 ID

        // when
        ApiResponse<AvailableLeagueDto> response = adminFootballDataWebService.addAvailableLeague(leagueId, "/api/admin/football/available/leagues/available");

        // then
        assertThat(response).isNotNull();
        assertThat(response.metaData().status()).isEqualTo("SUCCESS");
        assertThat(response.metaData().responseCode()).isEqualTo(200);
        assertThat(response.metaData().requestUrl()).isEqualTo("/api/admin/football/available/leagues/available");
        assertThat(response.response()).hasSize(1);
        AvailableLeagueDto addedLeague = response.response()[0];
        assertThat(addedLeague.leagueId()).isEqualTo(leagueId);
    }

    @DisplayName("이용가능 리그 삭제 성공")
    @Test
    void success_deleteAvailableLeague() throws JsonProcessingException {
        // given
        long leagueId = LeagueId.EURO; // 예시 리그 ID

        // when
        ApiResponse<String> response = adminFootballDataWebService.deleteAvailableLeague(leagueId, "/api/admin/football/available/leagues/available");

        // then
        assertThat(response).isNotNull();
        assertThat(response.metaData().status()).isEqualTo("SUCCESS");
        assertThat(response.metaData().responseCode()).isEqualTo(200);
        assertThat(response.metaData().requestUrl()).isEqualTo("/api/admin/football/available/leagues/available");
        assertThat(response.response()).contains("리그 삭제 성공");
    }

    @DisplayName("이용가능 경기 명단 조회 성공")
    @Test
    void success_getAvailableFixtures() throws JsonProcessingException {
        // given
        long leagueId = LeagueId.EURO; // 예시 리그 ID
        ZonedDateTime date = ZonedDateTime.now(); // 현재 날짜

        // when
        ApiResponse<AvailableFixtureDto> response = adminFootballDataWebService.getAvailableFixtures(leagueId, date, "/api/admin/football/available/fixtures/available");
        log.info("response={}", jacksonObjectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(response));

        // then
        assertThat(response).isNotNull();
        assertThat(response.metaData().status()).isEqualTo("SUCCESS");
        assertThat(response.metaData().responseCode()).isEqualTo(200);
        assertThat(response.metaData().requestUrl()).isEqualTo("/api/admin/football/available/fixtures/available");
    }

    @DisplayName("이용가능 경기 추가 성공")
    @Test
    void success_addAvailableFixture() throws JsonProcessingException {
        // given
        long fixtureId = FixtureId.FIXTURE_EURO2024_1; // 예시 경기 ID

        // when
        ApiResponse<AvailableFixtureDto> response = adminFootballDataWebService.addAvailableFixture(fixtureId, "/api/admin/football/available/fixtures/available");

        // then
        assertThat(response).isNotNull();
        assertThat(response.metaData().status()).isEqualTo("SUCCESS");
        assertThat(response.metaData().responseCode()).isEqualTo(200);
        assertThat(response.metaData().requestUrl()).isEqualTo("/api/admin/football/available/fixtures/available");
        assertThat(response.response()).hasSize(1);
        AvailableFixtureDto addedFixture = response.response()[0];
        assertThat(addedFixture.fixtureId()).isEqualTo(fixtureId);
    }

    @DisplayName("이용가능 경기 삭제 성공")
    @Test
    void success_deleteAvailableFixture() throws JsonProcessingException {
        // given
        long fixtureId = FixtureId.FIXTURE_EURO2024_1; // 예시 경기 ID

        // when
        ApiResponse<String> response = adminFootballDataWebService.deleteAvailableFixture(fixtureId, "/api/admin/football/available/fixtures/available");

        // then
        assertThat(response).isNotNull();
        assertThat(response.metaData().status()).isEqualTo("SUCCESS");
        assertThat(response.metaData().responseCode()).isEqualTo(200);
        assertThat(response.metaData().requestUrl()).isEqualTo("/api/admin/football/available/fixtures/available");
        assertThat(response.response()).contains("경기 삭제 성공");
    }
}