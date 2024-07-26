package com.gyechunsik.scoreboard.web.football.controller;

import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import com.gyechunsik.scoreboard.web.football.request.FixtureOfLeagueRequest;
import com.gyechunsik.scoreboard.web.football.request.TeamsOfLeagueRequest;
import com.gyechunsik.scoreboard.web.football.response.TeamsOfLeagueResponse;
import com.gyechunsik.scoreboard.web.football.response.FixtureOfLeagueResponse;
import com.gyechunsik.scoreboard.web.football.response.LeagueResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.FixtureEventsResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.info.FixtureInfoResponse;
import com.gyechunsik.scoreboard.web.football.service.FootballStreamWebService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * Rest API 로 football stream 데이터 제공.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/football")
public class FootballStreamDataController {

    private final FootballStreamWebService footballStreamWebService;

    /**
     * 이용 가능한 리그 목록 조회
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
    public ResponseEntity<ApiResponse<FixtureOfLeagueResponse>> fixturesOnClosestDateFromNow(
            @ModelAttribute FixtureOfLeagueRequest request
    ) {
        final String requestUrl = "/api/football/fixtures/";
        return ResponseEntity.ok(footballStreamWebService.getFixturesOnClosestDate(requestUrl, request, ZonedDateTime.now()));
    }

    @GetMapping("/fixtures/date")
    public ResponseEntity<ApiResponse<FixtureOfLeagueResponse>> fixturesOnDate(
            @ModelAttribute FixtureOfLeagueRequest request,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        final String requestUrl = "/api/football/fixtures/date";
        ZonedDateTime seoulDateTime = date.atStartOfDay(ZoneId.of("Asia/Seoul"));
        return ResponseEntity.ok(footballStreamWebService.getFixturesOnDate(requestUrl, request, seoulDateTime));
    }

    /**
     * 리그에 속한 이용 가능한 경기 일정 조회
     * @param request { leagueId : 리그 ID }
     * @return ApiResponse<FixtureOfLeagueResponse> 리그에 속한 경기 일정
     */
    @GetMapping("/fixtures/available")
    public ResponseEntity<ApiResponse<FixtureOfLeagueResponse>> availableFixturesOnClosestDateFromNow(
            @ModelAttribute FixtureOfLeagueRequest request
    ) {
        final String requestUrl = "/api/football/fixtures/available";
        return ResponseEntity.ok(footballStreamWebService.getAvailableFixturesOnClosestDate(requestUrl, request, ZonedDateTime.now()));
    }

    @GetMapping("/fixtures/available/date")
    public ResponseEntity<ApiResponse<FixtureOfLeagueResponse>> availableFixturesOnDate(
            @ModelAttribute FixtureOfLeagueRequest request,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        final String requestUrl = "/api/football/fixtures/available/date";
        ZonedDateTime seoulDateTime = date.atStartOfDay(ZoneId.of("Asia/Seoul"));
        return ResponseEntity.ok(footballStreamWebService.getAvailableFixturesOnDate(requestUrl, request, seoulDateTime));
    }

    // TODO : URL 변경되었으므로, electron-app 이랑 adminpage 에서 이 url 쓰는 곳 있는지 검사 필요
    /**
     * {@link FixtureInfoResponse} 라이브 상태 및 이벤트 정보를 포함하여 Fixture 정보를 제공합니다.
     * @param fixtureId 경기 ID
     * @return ApiResponse<FixtureInfoResponse> 경기 정보
     */
    @GetMapping("/fixtures/info")
    public ResponseEntity<ApiResponse<FixtureInfoResponse>> fixturesInfo(@RequestParam long fixtureId) {
        final String requestUrl = "/api/football/fixtures";
        return ResponseEntity.ok(footballStreamWebService.getFixtureInfo(requestUrl, fixtureId));
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

}
