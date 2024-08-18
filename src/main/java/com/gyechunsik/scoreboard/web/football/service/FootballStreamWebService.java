package com.gyechunsik.scoreboard.web.football.service;

import com.gyechunsik.scoreboard.domain.football.FootballRoot;
import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.entity.Team;
import com.gyechunsik.scoreboard.domain.football.entity.live.FixtureEvent;
import com.gyechunsik.scoreboard.domain.football.entity.live.LiveStatus;
import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import com.gyechunsik.scoreboard.web.common.service.ApiCommonResponseService;
import com.gyechunsik.scoreboard.web.football.request.FixtureOfLeagueRequest;
import com.gyechunsik.scoreboard.web.football.request.TeamsOfLeagueRequest;
import com.gyechunsik.scoreboard.web.football.response.TeamsOfLeagueResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureEventsResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureInfoResponse;
import com.gyechunsik.scoreboard.web.football.response.FixtureOfLeagueResponse;
import com.gyechunsik.scoreboard.web.football.response.FootballStreamDtoMapper;
import com.gyechunsik.scoreboard.web.football.response.LeagueResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureLineupResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureLiveStatusResponse;
import com.gyechunsik.scoreboard.web.football.response.temp.PlayerSubIn;
import com.gyechunsik.scoreboard.web.football.response.temp.PlayerSubInRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class FootballStreamWebService {

    private final FootballRoot footballRoot;
    private final ApiCommonResponseService apiCommonResponseService;
    private final PlayerSubInRepository playerSubInRepository;

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

    /**
     * 해당 리그에서 특정 날짜의 모든 경기 일정을 조회합니다.
     */
    public ApiResponse<FixtureOfLeagueResponse> getFixturesOnDate(String requestUrl, FixtureOfLeagueRequest request, ZonedDateTime paramDate) {
        Map<String, String> params = createParams(request.leagueId(), paramDate);
        try {
            validateRequest(request, requestUrl, params);
            ZonedDateTime dateTime = validateAndTruncateDate(paramDate, requestUrl, params);
            List<Fixture> fixturesOfLeague = footballRoot.getFixturesOnDate(request.leagueId(), dateTime);
            return createSuccessResponse(fixturesOfLeague, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling getFixturesOnDate() leagueId : {}", request.leagueId(), e);
            return createFailureResponse("fixture 정보를 가져오는데 실패했습니다", requestUrl, params);
        }
    }

    /**
     * 해당 리그에서 주어진 날짜로 부터 가장 가까운 경기가 있는 날의 모든 경기를 조회합니다.
     */
    public ApiResponse<FixtureOfLeagueResponse> getFixturesOnClosestDate(String requestUrl, FixtureOfLeagueRequest request, ZonedDateTime paramDate) {
        Map<String, String> params = createParams(request.leagueId(), paramDate);
        try {
            validateRequest(request, requestUrl, params);
            ZonedDateTime dateTime = validateAndTruncateDate(paramDate, requestUrl, params);
            List<Fixture> fixturesOfLeague = footballRoot.getFixturesOnClosestDate(request.leagueId(), dateTime);
            return createSuccessResponse(fixturesOfLeague, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling getFixturesOnClosestDate() leagueId : {}", request.leagueId(), e);
            return createFailureResponse("fixture 정보를 가져오는데 실패했습니다", requestUrl, params);
        }
    }

    /**
     * 해당 리그에서 특정 날짜의 모든 Available 경기 일정을 조회합니다.
     */
    public ApiResponse<FixtureOfLeagueResponse> getAvailableFixturesOnDate(String requestUrl, FixtureOfLeagueRequest request, ZonedDateTime paramDate) {
        Map<String, String> params = createParams(request.leagueId(), paramDate);
        try {
            validateRequest(request, requestUrl, params);
            ZonedDateTime dateTime = validateAndTruncateDate(paramDate, requestUrl, params);
            List<Fixture> fixturesOfLeague = footballRoot.getAvailableFixturesOnDate(request.leagueId(), dateTime);
            return createSuccessResponse(fixturesOfLeague, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling getAvailableFixturesOnDate() leagueId : {}", request.leagueId(), e);
            return createFailureResponse("fixture 정보를 가져오는데 실패했습니다", requestUrl, params);
        }
    }

    /**
     * 해당 리그에서 주어진 날짜로 부터 가장 가까운 경기가 있는 날의 모든 Available 경기를 조회합니다.
     */
    public ApiResponse<FixtureOfLeagueResponse> getAvailableFixturesOnClosestDate(String requestUrl, FixtureOfLeagueRequest request, ZonedDateTime paramDate) {
        Map<String, String> params = createParams(request.leagueId(), paramDate);
        try {
            validateRequest(request, requestUrl, params);
            ZonedDateTime dateTime = validateAndTruncateDate(paramDate, requestUrl, params);
            List<Fixture> fixturesOfLeague = footballRoot.getAvailableFixturesOnClosestDate(request.leagueId(), dateTime);
            return createSuccessResponse(fixturesOfLeague, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling getAvailableFixturesOnClosestDate() leagueId : {}", request.leagueId(), e);
            return createFailureResponse("fixture 정보를 가져오는데 실패했습니다", requestUrl, params);
        }
    }

    // TODO : 아직 lineup 이나 event 가 cache 되지 않은 경우에는 해당 부분을 null 로 두도록 수정 필요
    /*
    java.lang.NullPointerException: Cannot invoke "java.lang.Integer.intValue()" because the return value of "com.gyechunsik.scoreboard.domain.football.entity.live.LiveStatus.getElapsed()" is null
        at com.gyechunsik.scoreboard.web.football.response.FootballStreamDtoMapper.toFixtureInfoResponse(FootballStreamDtoMapper.java:77) ~[main/:na]
        at com.gyechunsik.scoreboard.web.football.service.FootballStreamWebService.getFixtureInfo(FootballStreamWebService.java:84) ~[main/:na]
        at com.gyechunsik.scoreboard.web.football.controller.FootballStreamDataController.fixturesInfo(FootballStreamDataController.java:66) ~[main/:na]
     */

    // info, liveStatus, events, lineup, matchStatistics, playerRatings, playerStatistcs(one player)
    /**
     *
     * @param requestUrl
     * @param fixtureId
     * @return
     */
    public ApiResponse<FixtureInfoResponse> getFixtureInfo(String requestUrl, long fixtureId) {
        Map<String, String> params = Map.of("fixtureId", String.valueOf(fixtureId));
        log.info("getFixtureInfo. params={}", params);

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

    public ApiResponse<FixtureLiveStatusResponse> getFixtureLiveStatus(String requestUrl, long fixtureId) {
        Map<String, String> params = Map.of("fixtureId", String.valueOf(fixtureId));
        log.info("getFixtureLiveStatus. params={}", params);

        try {
            Optional<LiveStatus> optionalLiveStatus = footballRoot.getFixtureLiveStatus(fixtureId);
            if (optionalLiveStatus.isEmpty()) {
                return apiCommonResponseService.createFailureResponse("존재하지 않는 fixture 입니다", requestUrl, params);
            }
            LiveStatus liveStatus = optionalLiveStatus.get();
            FixtureLiveStatusResponse response = FootballStreamDtoMapper.toFixtureLiveStatusResponse(fixtureId, liveStatus);
            return apiCommonResponseService.createSuccessResponse(new FixtureLiveStatusResponse[]{response}, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling method getFixtureLiveStatus() fixtureId : {}", fixtureId, e);
            return apiCommonResponseService.createFailureResponse("라이브 상태 정보를 가져오는데 실패했습니다", requestUrl, params);
        }
    }

    public ApiResponse<FixtureEventsResponse> getFixtureEvents(String requestUrl, long fixtureId) {
        Map<String, String> params = Map.of("fixtureId", String.valueOf(fixtureId));
        log.info("getFixtureEvents. params={}", params);

        try {
            // TODO : playerSubIn 을 통해 player 가 in 인지 assist 가 in 인지 변경 가능하도록함
            log.info("find PlayerSubIn :: find id={}", fixtureId);
            Optional<PlayerSubIn> findPlayerSubIn = playerSubInRepository.findById(fixtureId);
            boolean playerIsSubIn;
            if(findPlayerSubIn.isEmpty()) {
                log.info("findPlayerSubIn is empty");
                playerIsSubIn = false;
            } else {
                playerIsSubIn = findPlayerSubIn.get().isSubIn;
            }
            log.info("playerIsSubIn={}", playerIsSubIn);

            List<FixtureEvent> events = footballRoot.getFixtureEvents(fixtureId);
            FixtureEventsResponse response =
                    // FootballStreamDtoMapper.toFixtureEventsResponse(fixtureId, events);
                    FootballStreamDtoMapper.toFixtureEventsResponse(fixtureId, events, playerIsSubIn);
            return apiCommonResponseService.createSuccessResponse(new FixtureEventsResponse[]{response}, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling method getFixtureEvents() fixtureId : {}", fixtureId, e);
            return apiCommonResponseService.createFailureResponse("이벤트 정보를 가져오는데 실패했습니다", requestUrl, params);
        }
    }

    public ApiResponse<FixtureLineupResponse> getFixtureLineup(String requestUrl, long fixtureId) {
        Map<String, String> params = Map.of("fixtureId", String.valueOf(fixtureId));
        log.info("getFixtureLineup. params={}", params);

        try {
            Fixture fixture = footballRoot.getFixtureWithEager(fixtureId)
                    .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 경기입니다."));
            FixtureLineupResponse response = FootballStreamDtoMapper.toFixtureLineupResponse(fixture);
            return apiCommonResponseService.createSuccessResponse(new FixtureLineupResponse[]{response}, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling method getFixtureLineup() fixtureId : {}", fixtureId, e);
            return apiCommonResponseService.createFailureResponse("라인업 정보를 가져오는데 실패했습니다", requestUrl, params);
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

    private ApiResponse<FixtureOfLeagueResponse> createFailureResponse(String message, String requestUrl, Map<String, String> params) {
        return apiCommonResponseService.createFailureResponse(message, requestUrl, params);
    }

    private Map<String, String> createParams(long leagueId, ZonedDateTime dateTime) {
        return Map.of("leagueId", String.valueOf(leagueId), "date", dateTime.toString());
    }

    private void validateRequest(FixtureOfLeagueRequest request, String requestUrl, Map<String, String> params) {
        if (request == null) {
            throw new IllegalArgumentException("리그 정보가 없습니다");
        }
    }

    private ZonedDateTime validateAndTruncateDate(ZonedDateTime paramDate, String requestUrl, Map<String, String> params) {
        if (paramDate == null) {
            throw new IllegalArgumentException("날짜 정보가 없습니다");
        }
        return paramDate.truncatedTo(ChronoUnit.DAYS);
    }

    private ApiResponse<FixtureOfLeagueResponse> createSuccessResponse(List<Fixture> fixturesOfLeague, String requestUrl, Map<String, String> params) {
        FixtureOfLeagueResponse[] array = fixturesOfLeague.stream()
                .map(FootballStreamDtoMapper::toFixtureOfLeagueResponse)
                .toArray(FixtureOfLeagueResponse[]::new);
        return apiCommonResponseService.createSuccessResponse(array, requestUrl, params);
    }

}
