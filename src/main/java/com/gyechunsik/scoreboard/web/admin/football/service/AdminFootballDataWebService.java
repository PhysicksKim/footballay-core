package com.gyechunsik.scoreboard.web.admin.football.service;

import com.gyechunsik.scoreboard.domain.football.FootballRoot;
import com.gyechunsik.scoreboard.domain.football.dto.FixtureInfoDto;
import com.gyechunsik.scoreboard.domain.football.dto.LeagueDto;
import com.gyechunsik.scoreboard.domain.football.dto.PlayerDto;
import com.gyechunsik.scoreboard.domain.football.dto.TeamDto;
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
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdminFootballDataWebService {

    private final FootballRoot footballRoot;
    private final ApiCommonResponseService apiCommonResponseService;

    public ApiResponse<AvailableLeagueDto> getAvailableLeagues(String requestUrl) {
        List<LeagueDto> availableLeagues = footballRoot.getAvailableLeagues();
        AvailableLeagueDto[] array = availableLeagues.stream()
                .map(FootballDtoMapper::toAvailableLeagueDto)
                .toArray(AvailableLeagueDto[]::new);
        return apiCommonResponseService.createSuccessResponse(array, requestUrl);
    }

    public ApiResponse<AvailableLeagueDto> addAvailableLeague(long leagueId, String requestUrl) {
        Map<String, String> params = Map.of("leagueId", String.valueOf(leagueId));
        LeagueDto league;
        try {
            league = footballRoot.addAvailableLeague(leagueId);
        } catch (Exception e) {
            log.error("error while adding available league :: {}", e.getMessage());
            return apiCommonResponseService.createFailureResponse("리그 추가 실패", requestUrl, params);
        }
        AvailableLeagueDto leagueDto = FootballDtoMapper.toAvailableLeagueDto(league);
        AvailableLeagueDto[] response = {leagueDto};
        return apiCommonResponseService.createSuccessResponse(response, requestUrl, params);
    }

    public ApiResponse<String> deleteAvailableLeague(long leagueId, String requestUrl) {
        Map<String, String> params = Map.of("leagueId", String.valueOf(leagueId));
        boolean isSuccess = footballRoot.removeAvailableLeague(leagueId);
        if (!isSuccess) {
            log.error("error while deleting available league :: leagueId={}", leagueId);
            return apiCommonResponseService.createFailureResponse("리그 삭제 실패", requestUrl, params);
        }
        return apiCommonResponseService.createSuccessResponse(new String[]{"리그 삭제 성공"}, requestUrl, params);
    }

    public ApiResponse<AvailableFixtureDto> getAvailableFixtures(long leagueId, ZonedDateTime date, String requestUrl) {
        Map<String, String> params = Map.of(
                "leagueId", String.valueOf(leagueId),
                "date", date.toString()
        );
        try {
            date = date.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
            List<FixtureInfoDto> availableFixtures = footballRoot.getAvailableFixturesOnClosestDate(leagueId, date);
            AvailableFixtureDto[] array = availableFixtures.stream()
                    .map(FootballDtoMapper::toAvailableFixtureDto)
                    .sorted(Comparator.comparing(AvailableFixtureDto::date))
                    .toArray(AvailableFixtureDto[]::new);
            return apiCommonResponseService.createSuccessResponse(array, requestUrl, params);
        } catch (Exception e) {
            log.error("error while getting available fixtures :: {}", e.getMessage());
            return apiCommonResponseService.createFailureResponse("경기 조회 실패", requestUrl, params);
        }
    }

    public ApiResponse<AvailableFixtureDto> addAvailableFixture(long fixtureId, String requestUrl) {
        Map<String, String> params = Map.of("fixtureId", String.valueOf(fixtureId));
        FixtureInfoDto fixtureInfoDto;
        try {
            fixtureInfoDto = footballRoot.addAvailableFixture(fixtureId);
        } catch (Exception e) {
            log.error("error while adding available fixture :: {}", e.getMessage());
            return apiCommonResponseService.createFailureResponse("경기 추가 실패", requestUrl, params);
        }
        AvailableFixtureDto fixtureDto = FootballDtoMapper.toAvailableFixtureDto(fixtureInfoDto);
        AvailableFixtureDto[] response = {fixtureDto};
        return apiCommonResponseService.createSuccessResponse(response, requestUrl, params);
    }

    public ApiResponse<String> deleteAvailableFixture(long fixtureId, String requestUrl) {
        Map<String, String> params = Map.of("fixtureId", String.valueOf(fixtureId));
        boolean isSuccess = footballRoot.removeAvailableFixture(fixtureId);
        if (!isSuccess) {
            log.error("error while deleting available fixture :: fixtureId={}", fixtureId);
            return apiCommonResponseService.createFailureResponse("경기 삭제 실패", requestUrl, params);
        }
        return apiCommonResponseService.createSuccessResponse(new String[]{"경기 삭제 성공"}, requestUrl, params);
    }

    // teams, player, fixtures 조회
    public ApiResponse<TeamResponse> getTeamsOfLeague(long leagueId, String requestUrl) {
        Map<String, String> params = Map.of("leagueId", String.valueOf(leagueId));
        TeamResponse[] teamResponses;
        try {
            teamResponses = footballRoot.getTeamsOfLeague(leagueId).stream()
                    .map(FootballDtoMapper::toTeamResponse)
                    .toArray(TeamResponse[]::new);
        } catch (Exception e) {
            log.error("error while getting teams of league :: {}", e.getMessage());
            return apiCommonResponseService.createFailureResponse("팀 조회 실패", requestUrl, params);
        }
        return apiCommonResponseService.createSuccessResponse(teamResponses, requestUrl, params);
    }

    public ApiResponse<PlayerResponse> getSquadOfTeam(long teamId, String requestUrl) {
        Map<String, String> params = Map.of("teamId", String.valueOf(teamId));
        PlayerResponse[] playerResponses;
        try {
            playerResponses = footballRoot.getSquadOfTeam(teamId).stream()
                    .map(FootballDtoMapper::toPlayerDto)
                    .toArray(PlayerResponse[]::new);
        } catch (Exception e) {
            log.error("error while getting squad of team :: {}", e.getMessage());
            return apiCommonResponseService.createFailureResponse("선수 조회 실패", requestUrl, params);
        }
        return apiCommonResponseService.createSuccessResponse(playerResponses, requestUrl, params);
    }

    public ApiResponse<PlayerResponse> getPlayerInfo(long playerId, String requestUrl) {
        Map<String, String> params = Map.of("playerId", String.valueOf(playerId));
        PlayerResponse playerResponse;
        try {
            playerResponse = FootballDtoMapper.toPlayerDto(footballRoot.getPlayer(playerId));
        } catch (Exception e) {
            log.error("error while getting player info :: {}", e.getMessage());
            return apiCommonResponseService.createFailureResponse("선수 조회 실패", requestUrl, params);
        }
        return apiCommonResponseService.createSuccessResponse(new PlayerResponse[]{playerResponse}, requestUrl, params);
    }

    public ApiResponse<FixtureResponse> getFixturesFromDate(long leagueId, ZonedDateTime date, String requestUrl) {
        Map<String, String> params = Map.of(
                "leagueId", String.valueOf(leagueId),
                "date", date.toString()
        );
        date = date.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        FixtureResponse[] fixtures;
        try {
            fixtures = footballRoot.getNextFixturesFromDate(leagueId, date).stream()
                    .map(FootballDtoMapper::toFixtureDto)
                    .sorted(Comparator.comparing(FixtureResponse::date))
                    .toArray(FixtureResponse[]::new);
        } catch (Exception e) {
            log.error("error while getting fixture info :: {}", e.getMessage());
            return apiCommonResponseService.createFailureResponse("경기 조회 실패", requestUrl, params);
        }
        return apiCommonResponseService.createSuccessResponse(fixtures, requestUrl, params);
    }

    public ApiResponse<Void> addTeamPlayerRelation(long teamId, long playerId, String requestUrl) {
        Map<String, String> params = Map.of(
                "teamId", String.valueOf(teamId),
                "playerId", String.valueOf(playerId)
        );
        boolean isSuccess = footballRoot.addTeamPlayerRelation(teamId, playerId);
        if (!isSuccess) {
            log.error("error while adding team-player relation :: teamId={}, playerId={}", teamId, playerId);
            return apiCommonResponseService.createFailureResponse("팀-선수 관계 추가 실패", requestUrl, params);
        }
        log.info("team-player relation added :: teamId={}, playerId={}", teamId, playerId);
        return apiCommonResponseService.createSuccessResponse(null, requestUrl, params);
    }

    public ApiResponse<Void> removeTeamPlayerRelation(long teamId, long playerId, String requestUrl) {
        Map<String, String> params = Map.of(
                "teamId", String.valueOf(teamId),
                "playerId", String.valueOf(playerId)
        );
        boolean isSuccess = footballRoot.removeTeamPlayerRelation(teamId, playerId);
        if (!isSuccess) {
            log.error("error while removing team-player relation :: teamId={}, playerId={}", teamId, playerId);
            return apiCommonResponseService.createFailureResponse("팀-선수 관계 삭제 실패", requestUrl, params);
        }
        log.info("team-player relation removed :: teamId={}, playerId={}", teamId, playerId);
        return apiCommonResponseService.createSuccessResponse(null, requestUrl, params);
    }

    public ApiResponse<TeamsOfPlayerResponse> getTeamsOfPlayer(long playerId, String requestUrl) {
        Map<String, String> params = Map.of("playerId", String.valueOf(playerId));
        try{
            PlayerDto player = footballRoot.getPlayer(playerId);
            List<TeamDto> teamsOfPlayer = footballRoot.getTeamsOfPlayer(playerId);
            TeamsOfPlayerResponse response = FootballDtoMapper.toTeamsOfPlayer(player, teamsOfPlayer);
            return apiCommonResponseService.createSuccessResponse(new TeamsOfPlayerResponse[]{response}, requestUrl, params);
        } catch (Exception e) {
            log.error("error while getting teams of player :: {}", e.getMessage());
            return apiCommonResponseService.createFailureResponse("선수의 팀 조회 실패", requestUrl, params);
        }
    }

    public ApiResponse<FixtureResponse> getFixturesOnDate(long leagueId, ZonedDateTime date, String requestUrl) {
        Map<String, String> params = Map.of(
                "leagueId", String.valueOf(leagueId),
                "date", date.toString()
        );
        date = date.withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        FixtureResponse[] fixtures;
        try {
            fixtures = footballRoot.getFixturesOnDate(leagueId, date).stream()
                    .map(FootballDtoMapper::toFixtureDto)
                    .sorted(Comparator.comparing(FixtureResponse::date))
                    .toArray(FixtureResponse[]::new);
        } catch (Exception e) {
            log.error("error while getting fixture info :: {}", e.getMessage());
            return apiCommonResponseService.createFailureResponse("경기 조회 실패", requestUrl, params);
        }
        return apiCommonResponseService.createSuccessResponse(fixtures, requestUrl, params);
    }
}
