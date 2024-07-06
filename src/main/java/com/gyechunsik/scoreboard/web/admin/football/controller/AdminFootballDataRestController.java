package com.gyechunsik.scoreboard.web.admin.football.controller;

import com.gyechunsik.scoreboard.web.admin.football.request.FixtureIdRequest;
import com.gyechunsik.scoreboard.web.admin.football.request.LeagueIdRequest;
import com.gyechunsik.scoreboard.web.admin.football.response.*;
import com.gyechunsik.scoreboard.web.admin.football.service.AdminFootballDataWebService;
import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/football")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFootballDataRestController {

    /**
     * - Available Leagues 조회
     * GET : 가능 리그 조회
     * POST : 가능 리그 추가
     * DELETE : 가능 리그 삭제
     * - Available _Fixtures 조회
     * GET : 가능 팀 조회
     * POST : 가능 팀 추가
     * DELETE : 가능 팀 삭제
     */
    private final AdminFootballDataWebService adminFootballDataWebService;

    @GetMapping("/leagues/available")
    public ResponseEntity<ApiResponse<AvailableLeagueDto>> getAvailableLeagues() {
        final String requestUrl = "/api/admin/football/leagues/available";
        ApiResponse<AvailableLeagueDto> availableLeagues = adminFootballDataWebService.getAvailableLeagues(
                requestUrl
        );
        return ResponseEntity.ok().body(availableLeagues);
    }

    @PostMapping("/leagues/available")
    public ResponseEntity<ApiResponse<AvailableLeagueDto>> addAvailableLeague(@RequestBody LeagueIdRequest leagueIdRequest) {
        final long leagueId = leagueIdRequest.leagueId();
        String requestUrl = "/api/admin/football/leagues/available";
        ApiResponse<AvailableLeagueDto> response = adminFootballDataWebService.addAvailableLeague(leagueId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/leagues/available")
    public ResponseEntity<ApiResponse<String>> deleteAvailableLeague(@RequestParam long leagueId) {
        String requestUrl = "/api/admin/football/leagues/available";
        ApiResponse<String> response = adminFootballDataWebService.deleteAvailableLeague(leagueId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/fixtures/available")
    public ResponseEntity<ApiResponse<AvailableFixtureDto>> getAvailableFixtures(
            @RequestParam long leagueId,
            @RequestParam ZonedDateTime date
    ) {
        String requestUrl = "/api/admin/football/fixtures/available";
        ZonedDateTime now = ZonedDateTime.now();
        log.info("now : {}", now);
        ApiResponse<AvailableFixtureDto> response = adminFootballDataWebService.getAvailableFixtures(leagueId, date, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/fixtures/available")
    public ResponseEntity<ApiResponse<AvailableFixtureDto>> addAvailableFixture(
            @RequestBody FixtureIdRequest fixtureIdRequest
    ) {
        final long fixtureId = fixtureIdRequest.fixtureId();
        log.info("Add available fixture :: fixtureId : {}", fixtureId);
        String requestUrl = "/api/admin/football/fixtures/available";
        ApiResponse<AvailableFixtureDto> response = adminFootballDataWebService.addAvailableFixture(fixtureId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/fixtures/available")
    public ResponseEntity<ApiResponse<String>> deleteAvailableFixture(@RequestParam long fixtureId) {
        log.info("Delete available fixture :: fixtureId : {}", fixtureId);
        String requestUrl = "/api/admin/football/fixtures/available";
        ApiResponse<String> response = adminFootballDataWebService.deleteAvailableFixture(fixtureId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    /**
     * league, team, player, fixtures 조회
     */
    @GetMapping("/teams")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeamsOfLeague(long leagueId) {
        String requestUrl = "/api/admin/football/teams";
        ApiResponse<TeamResponse> response = adminFootballDataWebService.getTeamsOfLeague(leagueId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/teams/squad")
    public ResponseEntity<ApiResponse<PlayerResponse>> getSquadOfTeam(long teamId) {
        String requestUrl = "/api/admin/football/teams/squad";
        ApiResponse<PlayerResponse> response = adminFootballDataWebService.getSquadOfTeam(teamId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/players")
    public ResponseEntity<ApiResponse<PlayerResponse>> getPlayerInfo(long playerId) {
        String requestUrl = "/api/admin/football/players";
        ApiResponse<PlayerResponse> response = adminFootballDataWebService.getPlayerInfo(playerId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/fixtures")
    public ResponseEntity<ApiResponse<FixtureResponse>> getFixturesInfo(long leagueId,
                                                                        ZonedDateTime date
    ) {
        String requestUrl = "/api/admin/football/fixtures";
        ApiResponse<FixtureResponse> response = adminFootballDataWebService.getFixturesFromDate(leagueId, date, requestUrl);
        return ResponseEntity.ok().body(response);
    }

}
