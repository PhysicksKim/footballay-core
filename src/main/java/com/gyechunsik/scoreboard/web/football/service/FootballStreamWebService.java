package com.gyechunsik.scoreboard.web.football.service;

import com.gyechunsik.scoreboard.domain.football.FootballRoot;
import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.entity.live.FixtureEvent;
import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import com.gyechunsik.scoreboard.web.common.service.ApiCommonResponseService;
import com.gyechunsik.scoreboard.web.football.request.FixtureOfLeagueRequest;
import com.gyechunsik.scoreboard.web.football.request.TeamsOfLeagueRequest;
import com.gyechunsik.scoreboard.web.football.response.TeamsOfLeagueResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureEventsResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.info.FixtureInfoResponse;
import com.gyechunsik.scoreboard.web.football.response.FixtureOfLeagueResponse;
import com.gyechunsik.scoreboard.web.football.response.FootballStreamDtoMapper;
import com.gyechunsik.scoreboard.web.football.response.LeagueResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.lang.reflect.Array;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class FootballStreamWebService {

    private final FootballRoot footballRoot;
    private final ApiCommonResponseService apiCommonResponseService;

    public ApiResponse<LeagueResponse> getLeagueList(String requestUrl) {
        log.info("getLeagueList");

        try {
            List<League> availableLeagues = footballRoot.getAvailableLeagues();
            LeagueResponse[] array = availableLeagues.stream()
                    .map(FootballStreamDtoMapper::toLeagueResponse)
                    .toArray(LeagueResponse[]::new);

            return apiCommonResponseService.createSuccessResponse(array, requestUrl);
        } catch (Exception e) {
            log.error("Error occurred while calling method getLeagueList()", e);
            return apiCommonResponseService.createFailureResponse("리그 정보를 가져오는데 실패했습니다", requestUrl);
        }
    }

    public ApiResponse<FixtureOfLeagueResponse> getFixturesOfLeague(String requestUrl, FixtureOfLeagueRequest request) {
        final long leagueId = request.leagueId();
        Map<String, String> params = Map.of("leagueId", String.valueOf(leagueId));
        log.info("getFixturesOfLeague parameters={}", params);
        ZonedDateTime today = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS);

        try {
            List<Fixture> fixturesOfLeague = footballRoot.getAvailableFixtures(leagueId, today);
            FixtureOfLeagueResponse[] array = fixturesOfLeague.stream()
                    .map(FootballStreamDtoMapper::toFixtureOfLeagueResponse)
                    .toArray(FixtureOfLeagueResponse[]::new);

            return apiCommonResponseService.createSuccessResponse(array, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling method getFixturesOfLeague() leagueId : {}", request.leagueId(), e);
            return apiCommonResponseService.createFailureResponse("fixture 정보를 가져오는데 실패했습니다", requestUrl, params);
        }
    }

    public ApiResponse<FixtureInfoResponse> getFixtureInfo(String requestUrl, long fixtureId) {
        Map<String, String> params = Map.of("fixtureId", String.valueOf(fixtureId));
        log.info("getFixtureInfo. params={}", params);

        // TODO : 아직 lineup 이나 event 가 cache 되지 않은 경우에는 해당 부분을 null 로 두도록 수정 필요
        /*
        java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "com.gyechunsik.scoreboard.domain.football.entity.live.LiveStatus.getElapsed()" is null
            at com.gyechunsik.scoreboard.web.football.response.FootballStreamDtoMapper.toFixtureInfoResponse(FootballStreamDtoMapper.java:77) ~[main/:na]
            at com.gyechunsik.scoreboard.web.football.service.FootballStreamWebService.getFixtureInfo(FootballStreamWebService.java:84) ~[main/:na]
            at com.gyechunsik.scoreboard.web.football.controller.FootballStreamDataController.fixturesInfo(FootballStreamDataController.java:66) ~[main/:na]
         */
        try {
            Optional<Fixture> optionalFixtureWithEager = footballRoot.getFixtureWithEager(fixtureId);
            if (optionalFixtureWithEager.isEmpty()) {
                return apiCommonResponseService.createFailureResponse("존재하지 않는 fixture 입니다", requestUrl, params);
            }
            Fixture fixture = optionalFixtureWithEager.get();
            FixtureInfoResponse response =
                    FootballStreamDtoMapper.toFixtureInfoResponse(fixture);
            return apiCommonResponseService.createSuccessResponse(new FixtureInfoResponse[]{response}, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling method getFixtureInfo() fixtureId : {}", fixtureId, e);
            return apiCommonResponseService.createFailureResponse("fixture 정보를 가져오는데 실패했습니다", requestUrl, params);
        }
    }

    public ApiResponse<FixtureEventsResponse> getFixtureEvents(String requestUrl, long fixtureId) {
        Map<String, String> params = Map.of("fixtureId", String.valueOf(fixtureId));
        log.info("getFixtureEvents. params={}", params);

        try {
            List<FixtureEvent> events = footballRoot.getFixtureEvents(fixtureId);
            FixtureEventsResponse response =
                    FootballStreamDtoMapper.toFixtureEventsResponse(fixtureId, events);
            return apiCommonResponseService.createSuccessResponse(new FixtureEventsResponse[]{response}, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling method getFixtureEvents() fixtureId : {}", fixtureId, e);
            return apiCommonResponseService.createFailureResponse("이벤트 정보를 가져오는데 실패했습니다", requestUrl, params);
        }
    }

    public ApiResponse<TeamsOfLeagueResponse> getTeamsOfLeague(String requestUrl, TeamsOfLeagueRequest request) {
        final long leagueId = request.leagueId();
        Map<String, String> params = Map.of("leagueId", String.valueOf(leagueId));
        log.info("getTeamsOfLeague. parameters={}", params);

        try {
            List<Team> teamsOfLeague = footballRoot.getTeamsOfLeague(leagueId);
            TeamsOfLeagueResponse[] response = FootballStreamDtoMapper
                    .toTeamsOfLeagueResponseList(teamsOfLeague)
                    .toArray(TeamsOfLeagueResponse[]::new);

            return apiCommonResponseService.createSuccessResponse(response, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling method getTeamsOfLeague() leagueId : {}", request.leagueId(), e);
            return apiCommonResponseService.createFailureResponse("팀 정보를 가져오는데 실패했습니다", requestUrl, params);
        }
    }
}
