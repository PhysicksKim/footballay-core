package com.gyechunsik.scoreboard.web.football.controller;

import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import com.gyechunsik.scoreboard.web.common.dto.CachedApiResponse;
import com.gyechunsik.scoreboard.web.common.dto.MetaData;
import com.gyechunsik.scoreboard.web.common.service.ApiCommonResponseService;
import com.gyechunsik.scoreboard.web.common.service.CachedApiResponseService;
import com.gyechunsik.scoreboard.web.football.request.FixtureOfLeagueRequest;
import com.gyechunsik.scoreboard.web.football.request.TeamsOfLeagueRequest;
import com.gyechunsik.scoreboard.web.football.response.FixtureOfLeagueResponse;
import com.gyechunsik.scoreboard.web.football.response.LeagueResponse;
import com.gyechunsik.scoreboard.web.football.response.MatchStatisticsResponse;
import com.gyechunsik.scoreboard.web.football.response.TeamsOfLeagueResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureEventsResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureInfoResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureLineupResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureLiveStatusResponse;
import com.gyechunsik.scoreboard.web.football.service.FootballStreamWebService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Rest API 로 football stream 데이터 제공.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/football")
public class FootballStreamDataController {

    private final FootballStreamWebService footballStreamWebService;
    private final ApiCommonResponseService apiCommonResponseService;
    private final CachedApiResponseService cachedApiResponseService;

    /**
     * 이용 가능한 리그 목록 조회
     *
     * @return ApiResponse<LeagueResponse> 리그 목록
     */
    @GetMapping("/leagues/available")
    public ResponseEntity<ApiResponse<LeagueResponse>> leagueList() {
        final String requestUrl = "/api/football/leagues/available";
        return ResponseEntity.ok(footballStreamWebService.getLeagueList(requestUrl));
    }

    @GetMapping("/leagues/teams")
    public ResponseEntity<ApiResponse<TeamsOfLeagueResponse>> teamsOfLeague(@ModelAttribute TeamsOfLeagueRequest request) {
        final String requestUrl = "/api/football/leagues/teams";
        return ResponseEntity.ok(footballStreamWebService.getTeamsOfLeague(requestUrl, request));
    }

    @GetMapping("/fixtures")
    public ResponseEntity<ApiResponse<FixtureOfLeagueResponse>> fixturesOnNearestDateFromNow(
            @ModelAttribute FixtureOfLeagueRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        final String requestUrl = "/api/football/fixtures/";
        ZonedDateTime paramDate = date == null ? ZonedDateTime.now() : date.atStartOfDay(ZoneId.of("Asia/Seoul"));
        return ResponseEntity.ok(footballStreamWebService.getFixturesOnNearestDate(requestUrl, request, paramDate));
    }

    @GetMapping("/fixtures/date")
    public ResponseEntity<ApiResponse<FixtureOfLeagueResponse>> fixturesOnDate(
            @ModelAttribute FixtureOfLeagueRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        final String requestUrl = "/api/football/fixtures/date";
        if (date == null) {
            return ResponseEntity.ok(apiCommonResponseService.createFailureResponse("Parameter 'date' is required", requestUrl));
        }
        ZonedDateTime seoulDateTime = date.atStartOfDay(ZoneId.of("Asia/Seoul"));
        return ResponseEntity.ok(footballStreamWebService.getFixturesOnDate(requestUrl, request, seoulDateTime));
    }

    /**
     * 리그에 속한 이용 가능한 경기 일정 조회
     *
     * @param request { leagueId : 리그 ID }
     * @return ApiResponse<FixtureOfLeagueResponse> 리그에 속한 경기 일정
     */
    @GetMapping("/fixtures/available")
    public ResponseEntity<ApiResponse<FixtureOfLeagueResponse>> availableFixturesOnNearestDateFromNow(
            @ModelAttribute FixtureOfLeagueRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        final String requestUrl = "/api/football/fixtures/available";
        ZonedDateTime paramDate = date == null ? ZonedDateTime.now() : date.atStartOfDay(ZoneId.of("Asia/Seoul"));
        return ResponseEntity.ok(footballStreamWebService.getAvailableFixturesOnNearestDate(requestUrl, request, paramDate));
    }

    @GetMapping("/fixtures/available/date")
    public ResponseEntity<ApiResponse<FixtureOfLeagueResponse>> availableFixturesOnDate(
            @ModelAttribute FixtureOfLeagueRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        final String requestUrl = "/api/football/fixtures/available/date";
        if (date == null) {
            return ResponseEntity.ok(apiCommonResponseService.createFailureResponse("Parameter 'date' is required", requestUrl));
        }
        ZonedDateTime seoulDateTime = date.atStartOfDay(ZoneId.of("Asia/Seoul"));
        return ResponseEntity.ok(footballStreamWebService.getAvailableFixturesOnDate(requestUrl, request, seoulDateTime));
    }

    /**
     * {@link FixtureInfoResponse} 라이브 상태 및 이벤트 정보를 포함하여 Fixture 정보를 제공합니다.
     *
     * @param fixtureId 경기 ID
     * @return ApiResponse<FixtureInfoResponse> 경기 정보
     */
    @GetMapping("/fixtures/info")
    public ResponseEntity<ApiResponse<FixtureInfoResponse>> fixturesInfo(@RequestParam long fixtureId) {
        final String requestUrl = "/api/football/fixtures";
        return ResponseEntity.ok(footballStreamWebService.getFixtureInfo(requestUrl, fixtureId));
    }

    @GetMapping("/fixtures/live-status")
    public ResponseEntity<ApiResponse<FixtureLiveStatusResponse>> fixturesLiveStatus(@RequestParam long fixtureId) {
        final String requestUrl = "/api/football/fixtures/live-status";
        return ResponseEntity.ok(footballStreamWebService.getFixtureLiveStatus(requestUrl, fixtureId));
    }

    /**
     * 경기 이벤트 정보를 제공합니다.
     *
     * @param fixtureId 경기 ID
     * @return ApiResponse<FixtureEvent> 경기 이벤트 정보
     */
    @GetMapping("/fixtures/events")
    public ResponseEntity<ApiResponse<FixtureEventsResponse>> fixturesEvents(@RequestParam long fixtureId) {
        final String requestUrl = "/api/football/fixtures/events";
        return ResponseEntity.ok(footballStreamWebService.getFixtureEvents(requestUrl, fixtureId));
    }

    @GetMapping("/fixtures/lineup")
    public ResponseEntity<ApiResponse<FixtureLineupResponse>> fixturesLineup(
            @RequestParam long fixtureId,
            @RequestParam(required = false) String preferenceKey
    ) {
        final String requestUrl = "/api/football/fixtures/lineup";
        return ResponseEntity.ok(footballStreamWebService.getFixtureLineup(requestUrl, preferenceKey, fixtureId));
    }

    // TODO : prefkey 추가
    @GetMapping("/fixtures/statistics")
    public ResponseEntity<?> fixturesStatistics(
            @RequestParam long fixtureId,
            @RequestParam(required = false) String preferenceKey
    ) {
        final String requestUrl = "/api/football/fixtures/statistics";
        try {
            Optional<String> cachedResponseIfExist = cachedApiResponseService.getCachedResponseIfExist(requestUrl, Map.of("fixtureId", String.valueOf(fixtureId)));
            if(cachedResponseIfExist.isPresent()) {
                MetaData successMetaData = apiCommonResponseService.createSuccessMetaData(
                        requestUrl,
                        Map.of("fixtureId", String.valueOf(fixtureId))
                );
                CachedApiResponse cacheResp = new CachedApiResponse(successMetaData, cachedResponseIfExist.get());
                log.info("Cache hit [requestId={}] of fixtureStatistics for fixtureId: {}", successMetaData.requestId(), fixtureId);
                return ResponseEntity.ok()
                        .header("X-Cache", "HIT")
                        .body(cacheResp);
            }
        } catch (Exception e) {
            log.error("Error while checking cache: ", e);
        }
        return ResponseEntity.ok(footballStreamWebService.getMatchStatistics(requestUrl, preferenceKey, fixtureId));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponse<Void>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException ex,
            HttpServletRequest request
    ) {
        String requestUrl = request.getRequestURL().toString();
        Map<String, String> requestParams = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> String.join(",", entry.getValue())));

        log.info("Request Parameter Exception occurred");
        log.info("Request URL : {} , Params : {}", requestUrl, requestParams);
        log.info("Exception: ", ex);

        ApiResponse<Void> failureResponse = apiCommonResponseService.createFailureResponse(
                "Required request parameter is missing: " + ex.getParameterName(),
                requestUrl,
                requestParams
        );
        return ResponseEntity.badRequest().body(failureResponse);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException ex,
            HttpServletRequest request
    ) {
        String requestUrl = request.getRequestURL().toString();
        Map<String, String> requestParams = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> String.join(",", entry.getValue())));

        log.info("Method Argument Type Mismatch Exception occurred");
        log.info("Request URL : {} , Params : {}", requestUrl, requestParams);
        log.info("Exception: ", ex);

        ApiResponse<Void> failureResponse = apiCommonResponseService.createFailureResponse(
                "Invalid request parameter: " + ex.getName() + " with value " + ex.getValue(),
                requestUrl,
                requestParams
        );
        return ResponseEntity.badRequest().body(failureResponse);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex,
            HttpServletRequest request
    ) {
        String requestUrl = request.getRequestURL().toString();
        Map<String, String> requestParams = request.getParameterMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> String.join(",", entry.getValue())));

        log.info("Unexpected Exception occurred");
        log.info("Request URL : {} , Params : {}", requestUrl, requestParams);
        log.error("Exception: ", ex);

        ApiResponse<Void> failureResponse = apiCommonResponseService.createFailureResponse(
                "An unexpected error occurred",
                requestUrl,
                requestParams
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(failureResponse);
    }
}
