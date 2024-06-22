package com.gyechunsik.scoreboard.web.admin.football.service;

import com.gyechunsik.scoreboard.domain.football.FootballRoot;
import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.web.admin.football.response.*;
import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import com.gyechunsik.scoreboard.web.common.service.ApiCommonResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
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
        date = date.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        List<Fixture> availableFixtures = footballRoot.getAvailableFixtures(leagueId, date);
        AvailableFixtureDto[] array = availableFixtures.stream()
                .map(FootballDtoMapper::toAvailableFixtureDto)
                .sorted(Comparator.comparing(AvailableFixtureDto::date))
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

    // teams, player, fixtures 조회
    public ApiResponse<TeamDto> getTeamsOfLeague(long leagueId, String requestUrl) {
        TeamDto[] teamDtos;
        try {
            teamDtos = footballRoot.getTeamsOfLeague(leagueId).stream()
                    .map(FootballDtoMapper::toTeamDto)
                    .toArray(TeamDto[]::new);
        } catch (Exception e) {
            log.error("error while getting teams of league :: {}", e.getMessage());
            return apiCommonResponseService.createFailureResponse("팀 조회 실패", requestUrl);
        }
        return apiCommonResponseService.createSuccessResponse(teamDtos, requestUrl);
    }

    public ApiResponse<PlayerDto> getSquadOfTeam(long teamId, String requestUrl) {
        PlayerDto[] playerDtos;
        try {
            playerDtos = footballRoot.getSquadOfTeam(teamId).stream()
                    .map(FootballDtoMapper::toPlayerDto)
                    .toArray(PlayerDto[]::new);
        } catch (Exception e) {
            log.error("error while getting squad of team :: {}", e.getMessage());
            return apiCommonResponseService.createFailureResponse("선수 조회 실패", requestUrl);
        }
        return apiCommonResponseService.createSuccessResponse(playerDtos, requestUrl);
    }

    public ApiResponse<PlayerDto> getPlayerInfo(long playerId, String requestUrl) {
        PlayerDto playerDto;
        try {
            playerDto = FootballDtoMapper.toPlayerDto(footballRoot.getPlayer(playerId));
        } catch (Exception e) {
            log.error("error while getting player info :: {}", e.getMessage());
            return apiCommonResponseService.createFailureResponse("선수 조회 실패", requestUrl);
        }
        return apiCommonResponseService.createSuccessResponse(new PlayerDto[]{playerDto}, requestUrl);
    }

    public ApiResponse<FixtureDto> getFixturesFromDate(long leagueId, ZonedDateTime date, String requestUrl) {
        date = date.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        FixtureDto[] fixtures;
        try {
            fixtures = footballRoot.getNextFixturesFromDate(leagueId, date).stream()
                    .map(FootballDtoMapper::toFixtureDto)
                    .sorted(Comparator.comparing(FixtureDto::date))
                    .toArray(FixtureDto[]::new);
        } catch (Exception e) {
            log.error("error while getting fixture info :: {}", e.getMessage());
            return apiCommonResponseService.createFailureResponse("경기 조회 실패", requestUrl);
        }
        return apiCommonResponseService.createSuccessResponse(fixtures, requestUrl);
    }

}
