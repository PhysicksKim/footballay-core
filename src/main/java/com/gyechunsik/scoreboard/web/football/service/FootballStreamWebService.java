package com.gyechunsik.scoreboard.web.football.service;

import com.gyechunsik.scoreboard.domain.football.FootballRoot;
import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import com.gyechunsik.scoreboard.web.common.service.ApiCommonResponseService;
import com.gyechunsik.scoreboard.web.football.request.FixtureOfLeagueRequest;
import com.gyechunsik.scoreboard.web.football.response.FixtureInfoResponse;
import com.gyechunsik.scoreboard.web.football.response.FixtureOfLeagueResponse;
import com.gyechunsik.scoreboard.web.football.response.FootballStreamDtoMapper;
import com.gyechunsik.scoreboard.web.football.response.LeagueResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class FootballStreamWebService {

    private final FootballRoot footballRoot;
    private final ApiCommonResponseService apiCommonResponseService;

    public ApiResponse<LeagueResponse> getLeagueList(String requestUrl) {
        log.info("getLeagueList");
        List<League> availableLeagues = footballRoot.getAvailableLeagues();
        LeagueResponse[] array = availableLeagues.stream()
                .map(FootballStreamDtoMapper::toLeagueResponse)
                .toArray(LeagueResponse[]::new);
        return apiCommonResponseService.createSuccessResponse(array, requestUrl);
    }

    public ApiResponse<FixtureOfLeagueResponse> getFixturesOfLeague(String requestUrl, FixtureOfLeagueRequest request) {
        final long leagueId = request.leagueId();
        log.info("getFixturesOfLeague. leagueId : {}", leagueId);
        ZonedDateTime today = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS);
        List<Fixture> fixturesOfLeague = footballRoot.getAvailableFixtures(leagueId, today);
        FixtureOfLeagueResponse[] array = fixturesOfLeague.stream()
                .map(FootballStreamDtoMapper::toFixtureOfLeagueResponse)
                .toArray(FixtureOfLeagueResponse[]::new);
        return apiCommonResponseService.createSuccessResponse(array, requestUrl);
    }

    public ApiResponse<FixtureInfoResponse> getFixtureInfo(String requestUrl, long fixtureId) {
        log.info("getFixtureInfo. fixtureId : {}", fixtureId);
        footballRoot.getFixture(fixtureId);
        // Fixture fixture = footballRoot.getFixture(fixtureId);
        // FixtureInfoResponse response = FootballStreamDtoMapper.toFixtureInfoResponse(fixture);
        // return apiCommonResponseService.createSuccessResponse(response, requestUrl);
        return null;
    }

}
