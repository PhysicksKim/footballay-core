package com.footballay.core.mockserver.controller;

import com.footballay.core.mockserver.service.MockScenarioService;
import com.footballay.core.web.common.dto.ApiResponse;
import com.footballay.core.web.football.request.FixtureOfLeagueRequest;
import com.footballay.core.web.football.response.FixtureOfLeagueResponse;
import com.footballay.core.web.football.response.LeagueResponse;
import com.footballay.core.web.football.response.MatchStatisticsResponse;
import com.footballay.core.web.football.response.fixture.FixtureEventsResponse;
import com.footballay.core.web.football.response.fixture.FixtureInfoResponse;
import com.footballay.core.web.football.response.fixture.FixtureLineupResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * Mock Server용 Football Stream Controller
 * Desktop App 개발을 위한 Read-only 엔드포인트 제공
 */
@RestController
@RequestMapping("/api/football")
@Profile("mockserver")
public class MockFootballStreamController {
    private static final Logger log = LoggerFactory.getLogger(MockFootballStreamController.class);

    private final MockScenarioService mockScenarioService;

    public MockFootballStreamController(MockScenarioService mockScenarioService) {
        this.mockScenarioService = mockScenarioService;
    }

    /**
     * 이용 가능한 리그 목록 조회
     */
    @GetMapping("/leagues/available")
    public ResponseEntity<ApiResponse<LeagueResponse>> leagueList() {
        final String requestUrl = "/api/football/leagues/available";
        log.info("[Mock] GET {}", requestUrl);
        return ResponseEntity.ok(mockScenarioService.getLeagueList(requestUrl));
    }

    /**
     * 경기 목록 조회 (가장 가까운 날짜)
     */
    @GetMapping("/fixtures")
    public ResponseEntity<ApiResponse<FixtureOfLeagueResponse>> fixturesOnNearestDateFromNow(
            @ModelAttribute FixtureOfLeagueRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        final String requestUrl = "/api/football/fixtures";
        Map<String, String> params = new HashMap<>();
        if (request.leagueId() != null) {
            params.put("leagueId", String.valueOf(request.leagueId()));
        }
        if (date != null) {
            params.put("date", date.toString());
        }
        log.info("[Mock] GET {} with params: {}", requestUrl, params);
        return ResponseEntity.ok(mockScenarioService.getFixtures(requestUrl, params));
    }

    /**
     * 경기 목록 조회 (특정 날짜)
     */
    @GetMapping("/fixtures/date")
    public ResponseEntity<ApiResponse<FixtureOfLeagueResponse>> fixturesOnDate(
            @ModelAttribute FixtureOfLeagueRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        final String requestUrl = "/api/football/fixtures/date";
        Map<String, String> params = new HashMap<>();
        if (request.leagueId() != null) {
            params.put("leagueId", String.valueOf(request.leagueId()));
        }
        if (date != null) {
            params.put("date", date.toString());
        }
        log.info("[Mock] GET {} with params: {}", requestUrl, params);
        return ResponseEntity.ok(mockScenarioService.getFixtures(requestUrl, params));
    }

    /**
     * Available 경기 목록 조회
     */
    @GetMapping("/fixtures/available")
    public ResponseEntity<ApiResponse<FixtureOfLeagueResponse>> availableFixturesOnNearestDateFromNow(
            @ModelAttribute FixtureOfLeagueRequest request,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        final String requestUrl = "/api/football/fixtures/available";
        Map<String, String> params = new HashMap<>();
        if (request.leagueId() != null) {
            params.put("leagueId", String.valueOf(request.leagueId()));
        }
        if (date != null) {
            params.put("date", date.toString());
        }
        log.info("[Mock] GET {} with params: {}", requestUrl, params);
        return ResponseEntity.ok(mockScenarioService.getFixtures(requestUrl, params));
    }

    /**
     * 경기 정보 조회 (시간 흐름 시뮬레이션)
     */
    @GetMapping("/fixtures/info")
    public ResponseEntity<ApiResponse<FixtureInfoResponse>> fixturesInfo(@RequestParam long fixtureId) {
        final String requestUrl = "/api/football/fixtures/info";
        Map<String, String> params = Map.of("fixtureId", String.valueOf(fixtureId));
        log.info("[Mock] GET {} with fixtureId={}", requestUrl, fixtureId);
        return ResponseEntity.ok(mockScenarioService.getFixtureInfo(fixtureId, requestUrl, params));
    }

    /**
     * 경기 이벤트 조회 (시간 흐름 시뮬레이션)
     */
    @GetMapping("/fixtures/events")
    public ResponseEntity<ApiResponse<FixtureEventsResponse>> fixturesEvents(@RequestParam long fixtureId) {
        final String requestUrl = "/api/football/fixtures/events";
        Map<String, String> params = Map.of("fixtureId", String.valueOf(fixtureId));
        log.info("[Mock] GET {} with fixtureId={}", requestUrl, fixtureId);
        return ResponseEntity.ok(mockScenarioService.getFixtureEvents(fixtureId, requestUrl, params));
    }

    /**
     * 라인업 조회 (시간 흐름 시뮬레이션)
     */
    @GetMapping("/fixtures/lineup")
    public ResponseEntity<ApiResponse<FixtureLineupResponse>> fixturesLineup(
            @RequestParam long fixtureId,
            @RequestParam(required = false) String preferenceKey) {
        final String requestUrl = "/api/football/fixtures/lineup";
        Map<String, String> params = new HashMap<>();
        params.put("fixtureId", String.valueOf(fixtureId));
        if (preferenceKey != null) {
            params.put("preferenceKey", preferenceKey);
        }
        log.info("[Mock] GET {} with fixtureId={}", requestUrl, fixtureId);
        return ResponseEntity.ok(mockScenarioService.getFixtureLineup(fixtureId, requestUrl, params));
    }

    /**
     * 경기 통계 조회 (시간 흐름 시뮬레이션)
     */
    @GetMapping("/fixtures/statistics")
    public ResponseEntity<ApiResponse<MatchStatisticsResponse>> fixturesStatistics(
            @RequestParam long fixtureId,
            @RequestParam(required = false) String preferenceKey) {
        final String requestUrl = "/api/football/fixtures/statistics";
        Map<String, String> params = new HashMap<>();
        params.put("fixtureId", String.valueOf(fixtureId));
        if (preferenceKey != null) {
            params.put("preferenceKey", preferenceKey);
        }
        log.info("[Mock] GET {} with fixtureId={}", requestUrl, fixtureId);
        return ResponseEntity.ok(mockScenarioService.getFixtureStatistics(fixtureId, requestUrl, params));
    }

    /**
     * Mock Admin API: 경기 시작 시간 리셋
     */
    @PostMapping("/mock/admin/fixtures/{fixtureId}/reset")
    public ResponseEntity<Map<String, String>> resetMatchTime(@PathVariable Long fixtureId) {
        mockScenarioService.resetMatchStartTime(fixtureId);
        return ResponseEntity.ok(Map.of(
                "status", "success",
                "message", "Match start time reset for fixtureId=" + fixtureId
        ));
    }
}
