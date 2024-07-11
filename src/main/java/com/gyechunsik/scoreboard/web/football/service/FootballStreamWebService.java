package com.gyechunsik.scoreboard.web.football.service;

import com.gyechunsik.scoreboard.domain.football.FootballRoot;
import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.live.FixtureEvent;
import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import com.gyechunsik.scoreboard.web.common.service.ApiCommonResponseService;
import com.gyechunsik.scoreboard.web.football.request.FixtureOfLeagueRequest;
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
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class FootballStreamWebService {

    private final FootballRoot footballRoot;
    private final ApiCommonResponseService apiCommonResponseService;

    public ApiResponse<LeagueResponse> getLeagueList(String requestUrl) {
        try {
            log.info("getLeagueList");
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
        try {
            final long leagueId = request.leagueId();
            log.info("getFixturesOfLeague. leagueId : {}", leagueId);
            ZonedDateTime today = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS);
            List<Fixture> fixturesOfLeague = footballRoot.getAvailableFixtures(leagueId, today);
            FixtureOfLeagueResponse[] array = fixturesOfLeague.stream()
                    .map(FootballStreamDtoMapper::toFixtureOfLeagueResponse)
                    .toArray(FixtureOfLeagueResponse[]::new);
            return apiCommonResponseService.createSuccessResponse(array, requestUrl);
        } catch (Exception e) {
            log.error("Error occurred while calling method getFixturesOfLeague() leagueId : {}", request.leagueId(), e);
            return apiCommonResponseService.createFailureResponse("fixture 정보를 가져오는데 실패했습니다", requestUrl);
        }
    }

    public ApiResponse<FixtureInfoResponse> getFixtureInfo(String requestUrl, long fixtureId) {
        try {

            log.info("getFixtureInfo. fixtureId : {}", fixtureId);
            Optional<Fixture> optionalFixtureWithEager = footballRoot.getFixtureWithEager(fixtureId);
            if (optionalFixtureWithEager.isEmpty()) {
                return apiCommonResponseService.createFailureResponse("존재하지 않는 fixture 입니다", requestUrl);
            }
            Fixture fixture = optionalFixtureWithEager.get();
            FixtureInfoResponse response =
                    FootballStreamDtoMapper.toFixtureInfoResponse(fixture);
            return apiCommonResponseService.createSuccessResponse(new FixtureInfoResponse[]{response}, requestUrl);
        } catch (Exception e) {
            log.error("Error occurred while calling method getFixtureInfo() fixtureId : {}", fixtureId, e);
            return apiCommonResponseService.createFailureResponse("fixture 정보를 가져오는데 실패했습니다", requestUrl);
        }
    }

    public ApiResponse<FixtureEventsResponse> getFixtureEvents(String requestUrl, long fixtureId) {
        try {
            log.info("getFixtureEvents. fixtureId : {}", fixtureId);
            List<FixtureEvent> events = footballRoot.getFixtureEvents(fixtureId);
            FixtureEventsResponse response =
                    FootballStreamDtoMapper.toFixtureEventsResponse(fixtureId, events);
            return apiCommonResponseService.createSuccessResponse(new FixtureEventsResponse[]{response}, requestUrl);
        } catch (Exception e) {
            log.error("Error occurred while calling method getFixtureEvents() fixtureId : {}", fixtureId, e);
            return apiCommonResponseService.createFailureResponse("이벤트 정보를 가져오는데 실패했습니다", requestUrl);
        }
    }
}
