package com.footballay.core.web.football.service;

import com.footballay.core.domain.football.FootballRoot;
import com.footballay.core.domain.football.dto.*;
import com.footballay.core.domain.football.preference.FootballPreferenceService;
import com.footballay.core.web.common.dto.ApiResponse;
import com.footballay.core.web.common.service.ApiCommonResponseService;
import com.footballay.core.web.common.service.CachedApiResponseService;
import com.footballay.core.web.football.request.FixtureOfLeagueRequest;
import com.footballay.core.web.football.request.TeamsOfLeagueRequest;
import com.footballay.core.web.football.response.*;
import com.footballay.core.web.football.response.fixture.FixtureEventsResponse;
import com.footballay.core.web.football.response.fixture.FixtureInfoResponse;
import com.footballay.core.web.football.response.fixture.FixtureLineupResponse;
import com.footballay.core.web.football.response.fixture.FixtureLiveStatusResponse;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class FootballStreamWebService {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FootballStreamWebService.class);
    private final FootballRoot footballRoot;
    private final ApiCommonResponseService apiCommonResponseService;
    private final FootballPreferenceService footballPreferenceService;
    private final CachedApiResponseService cachedApiResponseService;

    public ApiResponse<LeagueResponse> getLeagueList(String requestUrl) {
        log.info("getLeagueList");
        try {
            List<LeagueDto> availableLeagues = footballRoot.getAvailableLeagues();
            LeagueResponse[] array = availableLeagues.stream().map(FootballStreamDtoMapper::toLeagueResponse).toArray(LeagueResponse[]::new);
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
            List<FixtureInfoDto> fixturesOfLeague = footballRoot.getFixturesOnDate(request.leagueId(), dateTime);
            return createFixtureListSuccessResponse(fixturesOfLeague, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling getFixturesOnDate() leagueId : {}", request.leagueId(), e);
            return createFailureResponse("fixture 정보를 가져오는데 실패했습니다", requestUrl, params);
        }
    }

    /**
     * 해당 리그에서 주어진 날짜로 부터 가장 가까운 경기가 있는 날의 모든 경기를 조회합니다.
     */
    public ApiResponse<FixtureOfLeagueResponse> getFixturesOnNearestDate(String requestUrl, FixtureOfLeagueRequest request, ZonedDateTime paramDate) {
        Map<String, String> params = createParams(request.leagueId(), paramDate);
        try {
            validateRequest(request, requestUrl, params);
            ZonedDateTime dateTime = validateAndTruncateDate(paramDate, requestUrl, params);
            List<FixtureInfoDto> fixturesOfLeague = footballRoot.getFixturesOnNearestDate(request.leagueId(), dateTime);
            return createFixtureListSuccessResponse(fixturesOfLeague, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling getFixturesOnNearestDate() leagueId : {}", request.leagueId(), e);
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
            List<FixtureInfoDto> fixturesOfLeague = footballRoot.getAvailableFixturesOnDate(request.leagueId(), dateTime);
            return createFixtureListSuccessResponse(fixturesOfLeague, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling getAvailableFixturesOnDate() leagueId : {}", request.leagueId(), e);
            return createFailureResponse("fixture 정보를 가져오는데 실패했습니다", requestUrl, params);
        }
    }

    /**
     * 해당 리그에서 주어진 날짜로 부터 가장 가까운 경기가 있는 날의 모든 Available 경기를 조회합니다.
     */
    public ApiResponse<FixtureOfLeagueResponse> getAvailableFixturesOnNearestDate(String requestUrl, FixtureOfLeagueRequest request, ZonedDateTime paramDate) {
        Map<String, String> params = createParams(request.leagueId(), paramDate);
        try {
            validateRequest(request, requestUrl, params);
            ZonedDateTime dateTime = validateAndTruncateDate(paramDate, requestUrl, params);
            List<FixtureInfoDto> fixturesOfLeague = footballRoot.getAvailableFixturesOnNearestDate(request.leagueId(), dateTime);
            return createFixtureListSuccessResponse(fixturesOfLeague, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling getAvailableFixturesOnNearestDate() leagueId : {}", request.leagueId(), e);
            return createFailureResponse("fixture 정보를 가져오는데 실패했습니다", requestUrl, params);
        }
    }

    /**
     * 해당 경기의 정보를 조회합니다.
     *
     * @param requestUrl 요청 URL
     * @param fixtureId  경기 ID
     * @return 경기 정보 응답
     */
    public ApiResponse<FixtureInfoResponse> getFixtureInfo(String requestUrl, long fixtureId) {
        Map<String, String> params = Map.of("fixtureId", String.valueOf(fixtureId));
        log.info("getFixtureInfo. params={}", params);
        try {
            Optional<FixtureInfoDto> optionalFixtureWithEager = footballRoot.getFixtureInfo(fixtureId);
            if (optionalFixtureWithEager.isEmpty()) {
                return apiCommonResponseService.createFailureResponse("존재하지 않는 fixture 입니다", requestUrl, params);
            }
            FixtureInfoDto fixtureInfoDto = optionalFixtureWithEager.get();
            FixtureInfoResponse response = FootballStreamDtoMapper.toFixtureInfoResponse(fixtureInfoDto);
            return apiCommonResponseService.createSuccessResponse(new FixtureInfoResponse[] {response}, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling method getFixtureInfo() fixtureId : {}", fixtureId, e);
            return apiCommonResponseService.createFailureResponse("fixture 정보를 가져오는데 실패했습니다", requestUrl, params);
        }
    }

    public ApiResponse<FixtureLiveStatusResponse> getFixtureLiveStatus(String requestUrl, long fixtureId) {
        Map<String, String> params = Map.of("fixtureId", String.valueOf(fixtureId));
        log.info("getFixtureLiveStatus. params={}", params);
        try {
            Optional<LiveStatusDto> optionalLiveStatus = footballRoot.getFixtureLiveStatus(fixtureId);
            if (optionalLiveStatus.isEmpty()) {
                return apiCommonResponseService.createFailureResponse("존재하지 않는 fixture 입니다", requestUrl, params);
            }
            LiveStatusDto liveStatus = optionalLiveStatus.get();
            FixtureLiveStatusResponse response = FootballStreamDtoMapper.toFixtureLiveStatusResponse(fixtureId, liveStatus);
            return apiCommonResponseService.createSuccessResponse(new FixtureLiveStatusResponse[] {response}, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling method getFixtureLiveStatus() fixtureId : {}", fixtureId, e);
            return apiCommonResponseService.createFailureResponse("라이브 상태 정보를 가져오는데 실패했습니다", requestUrl, params);
        }
    }

    public ApiResponse<FixtureEventsResponse> getFixtureEvents(String requestUrl, long fixtureId) {
        Map<String, String> params = Map.of("fixtureId", String.valueOf(fixtureId));
        log.info("getFixtureEvents. params={}", params);
        try {
            List<FixtureEventWithPlayerDto> events = footballRoot.getFixtureEvents(fixtureId);
            FixtureEventsResponse response = FootballStreamDtoMapper.toFixtureEventsResponse(fixtureId, events);
            return apiCommonResponseService.createSuccessResponse(new FixtureEventsResponse[] {response}, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling method getFixtureEvents() fixtureId : {}", fixtureId, e);
            return apiCommonResponseService.createFailureResponse("이벤트 정보를 가져오는데 실패했습니다", requestUrl, params);
        }
    }

    public ApiResponse<FixtureLineupResponse> getFixtureLineup(String requestUrl, @Nullable String preferenceKey, long fixtureId) {
        Map<String, String> params = new HashMap<>();
        params.put("fixtureId", String.valueOf(fixtureId));
        if (preferenceKey != null) {
            params.put("preferenceKey", preferenceKey);
        }
        log.info("getFixtureLineup. params={}", params);
        try {
            FixtureWithLineupDto fixture = footballRoot.getFixtureWithLineup(fixtureId).orElseThrow(() -> new IllegalArgumentException("라인업 응답이 비어있습니다. fixtureId=" + fixtureId));
            FixtureLineupResponse response = FootballStreamDtoMapper.toFixtureLineupResponse(fixture);
            if (StringUtils.hasText(preferenceKey) && response.lineup() != null) {
                log.info("lineup with Custom photo. preference key : {}", preferenceKey);
                Assert.notNull(response.lineup().away(), "away lineup is null");
                Assert.notNull(response.lineup().away(), "away lineup is null");
                Set<Long> startPlayerIds = Stream.concat(response.lineup().home().players().stream().map(FixtureLineupResponse._LineupPlayer::id), response.lineup().away().players().stream().map(FixtureLineupResponse._LineupPlayer::id)).collect(Collectors.toSet());
                Set<Long> substitutePlayerIds = Stream.concat(response.lineup().home().substitutes().stream().map(FixtureLineupResponse._LineupPlayer::id), response.lineup().away().substitutes().stream().map(FixtureLineupResponse._LineupPlayer::id)).collect(Collectors.toSet());
                Set<Long> playerIds = Stream.concat(startPlayerIds.stream(), substitutePlayerIds.stream()).collect(Collectors.toSet());
                Map<Long, String> photoMap = footballPreferenceService.getCustomPhotoUrlsOfPlayers(preferenceKey, playerIds);
                response = FootballStreamDtoMapper.copyWithCustomPhotos(response, photoMap);
            }
            return apiCommonResponseService.createSuccessResponse(new FixtureLineupResponse[] {response}, requestUrl, params);
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
            List<TeamDto> teamsOfLeague = footballRoot.getTeamsOfLeague(leagueId);
            TeamsOfLeagueResponse[] response = FootballStreamDtoMapper.toTeamsOfLeagueResponseList(teamsOfLeague).toArray(TeamsOfLeagueResponse[]::new);
            return apiCommonResponseService.createSuccessResponse(response, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling method getTeamsOfLeague() leagueId : {}", request.leagueId(), e);
            return apiCommonResponseService.createFailureResponse("팀 정보를 가져오는데 실패했습니다", requestUrl, params);
        }
    }

    public ApiResponse<MatchStatisticsResponse> getMatchStatistics(String requestUrl, @Nullable String preferenceKey, long fixtureId) {
        Map<String, String> params = Map.of("fixtureId", String.valueOf(fixtureId));
        log.info("getMatchStatistics. params={}", params);
        try {
            MatchStatisticsDto matchStatisticsDTO = footballRoot.getMatchStatistics(fixtureId);
            MatchStatisticsResponse responseData = MatchStatisticsResponseMapper.toResponse(matchStatisticsDTO);
            cachedApiResponseService.cacheResponse(requestUrl, params, responseData);
            return apiCommonResponseService.createSuccessResponse(new MatchStatisticsResponse[] {responseData}, requestUrl, params);
        } catch (Exception e) {
            log.error("Error occurred while calling method getMatchStatistics() fixtureId : {}", fixtureId, e);
            return apiCommonResponseService.createFailureResponse("경기 통계 정보를 가져오는데 실패했습니다", requestUrl, params);
        }
    }

    private ApiResponse<FixtureOfLeagueResponse> createFailureResponse(String message, String requestUrl, Map<String, String> params) {
        return apiCommonResponseService.createFailureResponse(message, requestUrl, params);
    }

    private Map<String, String> createParams(long leagueId, ZonedDateTime dateTime) {
        return Map.of("leagueId", String.valueOf(leagueId), "date", dateTime.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }

    private void validateRequest(FixtureOfLeagueRequest request, String requestUrl, Map<String, String> params) {
        if (request == null) {
            throw new IllegalArgumentException("리그 정보가 없습니다. requestUrl=" + requestUrl + ", params=" + params);
        }
    }

    private ZonedDateTime validateAndTruncateDate(ZonedDateTime paramDate, String requestUrl, Map<String, String> params) {
        if (paramDate == null) {
            throw new IllegalArgumentException("날짜 정보가 없습니다. requestUrl=" + requestUrl + ", params=" + params);
        }
        return paramDate.truncatedTo(ChronoUnit.DAYS);
    }

    private ApiResponse<FixtureOfLeagueResponse> createFixtureListSuccessResponse(List<FixtureInfoDto> fixturesOfLeague, String requestUrl, Map<String, String> params) {
        FixtureOfLeagueResponse[] array = fixturesOfLeague.stream().map(FootballStreamDtoMapper::toFixtureOfLeagueResponse).toArray(FixtureOfLeagueResponse[]::new);
        return apiCommonResponseService.createSuccessResponse(array, requestUrl, params);
    }

    public FootballStreamWebService(final FootballRoot footballRoot, final ApiCommonResponseService apiCommonResponseService, final FootballPreferenceService footballPreferenceService, final CachedApiResponseService cachedApiResponseService) {
        this.footballRoot = footballRoot;
        this.apiCommonResponseService = apiCommonResponseService;
        this.footballPreferenceService = footballPreferenceService;
        this.cachedApiResponseService = cachedApiResponseService;
    }
}
