package com.gyechunsik.scoreboard.web.admin.football.service;

import com.gyechunsik.scoreboard.domain.football.FootballRoot;
import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.web.admin.football.dto.*;
import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import com.gyechunsik.scoreboard.web.common.service.ApiCommonResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminFootballDataWebService {

    private final FootballRoot footballRoot;
    private final ApiCommonResponseService apiCommonResponseService;

    public ApiResponse<AvailableLeagueDto> getAvailableLeagues(String requestUrl) {
        List<League> availableLeagues = footballRoot.getAvailableLeagues();
        AvailableLeagueDto[] array = availableLeagues.stream()
                .map(FootballDtoMapper::toAvailableLeagueDto)
                .toArray(AvailableLeagueDto[]::new);
        return apiCommonResponseService.createSuccessResponse(array, requestUrl);
    }

    public ApiResponse<AvailableLeagueDto> addAvailableLeague(long leagueId, String requestUrl) {
        League league;
        try {
            league = footballRoot.addAvailableLeague(leagueId);
        } catch (Exception e) {
            log.error("error while adding available league :: {}", e.getMessage());
            return apiCommonResponseService.createFailureResponse("리그 추가 실패", requestUrl);
        }
        AvailableLeagueDto leagueDto = FootballDtoMapper.toAvailableLeagueDto(league);
        AvailableLeagueDto[] response = {leagueDto};
        return apiCommonResponseService.createSuccessResponse(response, requestUrl);
    }

    public ApiResponse<String> deleteAvailableLeague(long leagueId, String requestUrl) {
        boolean isSuccess = footballRoot.removeAvailableLeague(leagueId);
        if (!isSuccess) {
            log.error("error while deleting available league :: leagueId={}", leagueId);
            return apiCommonResponseService.createFailureResponse("리그 삭제 실패", requestUrl);
        }
        return apiCommonResponseService.createSuccessResponse(new String[]{"리그 삭제 성공"}, requestUrl);
    }

    public ApiResponse<AvailableFixtureDto> getAvailableFixtures(long leagueId, ZonedDateTime date, String requestUrl) {
        List<Fixture> availableFixtures = footballRoot.getAvailableFixtures(leagueId, date);
        AvailableFixtureDto[] array = availableFixtures.stream()
                .map(FootballDtoMapper::toAvailableFixtureDto)
                .toArray(AvailableFixtureDto[]::new);
        return apiCommonResponseService.createSuccessResponse(array, requestUrl);
    }

    public ApiResponse<AvailableFixtureDto> addAvailableFixture(long fixtureId, String requestUrl) {
        Fixture fixture;
        try {
            fixture = footballRoot.addAvailableFixture(fixtureId);
        } catch (Exception e) {
            log.error("error while adding available fixture :: {}", e.getMessage());
            return apiCommonResponseService.createFailureResponse("경기 추가 실패", requestUrl);
        }
        AvailableFixtureDto fixtureDto = FootballDtoMapper.toAvailableFixtureDto(fixture);
        AvailableFixtureDto[] response = {fixtureDto};
        return apiCommonResponseService.createSuccessResponse(response, requestUrl);
    }

    public ApiResponse<String> deleteAvailableFixture(long fixtureId, String requestUrl) {
        boolean isSuccess = footballRoot.removeAvailableFixture(fixtureId);
        if (!isSuccess) {
            log.error("error while deleting available fixture :: fixtureId={}", fixtureId);
            return apiCommonResponseService.createFailureResponse("경기 삭제 실패", requestUrl);
        }
        return apiCommonResponseService.createSuccessResponse(new String[]{"경기 삭제 성공"}, requestUrl);
    }
}
