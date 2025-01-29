package com.gyechunsik.scoreboard.web.admin.football.controller;

import com.gyechunsik.scoreboard.web.admin.football.request.CachePlayerSingleRequest;
import com.gyechunsik.scoreboard.web.admin.football.request.LeagueIdRequest;
import com.gyechunsik.scoreboard.web.admin.football.request.PreventUnlinkRequest;
import com.gyechunsik.scoreboard.web.admin.football.request.TeamIdRequest;
import com.gyechunsik.scoreboard.web.admin.football.response.ExternalApiStatusResponse;
import com.gyechunsik.scoreboard.web.admin.football.service.AdminFootballCacheWebService;
import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import com.gyechunsik.scoreboard.web.common.service.ApiV1CommonResponseService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/football/cache")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFootballCacheRestController {

    private final AdminFootballCacheWebService adminFootballCacheWebService;
    private final ApiV1CommonResponseService apiV1CommonResponseService;

    @GetMapping("/status")
    public ResponseEntity<?> getApiStatus() {
        final String requestUrl = "/api/admin/football/cache/status";
        log.info("get api status");
        ApiResponse<ExternalApiStatusResponse> apiStatus = adminFootballCacheWebService.getApiStatus(requestUrl);
        if(apiStatus.metaData().responseCode() != 200) {
            return ResponseEntity.badRequest().body(apiStatus);
        } else {
            return ResponseEntity.ok().body(apiStatus);
        }
    }

    /**
     * 1. 리그 : 단일 리그 - leagueId
     * 2. 리그 : 모든 current 리그 - .
     * 3. 팀 : 단일 팀 캐싱. 팀이 속한 모든 current league 캐싱 - teamId
     * 4. 팀 : 리그에 속한 팀 - leagueId
     * 5. 선수 : 팀의 선수단 캐싱 - teamId
     * 6. 일정 : 리그의 일정 캐싱 - leagueId
     */

    @PostMapping("/leagues")
    public ResponseEntity<ApiResponse<Void>> cacheLeague(@RequestBody LeagueIdRequest leagueIdRequest) {
        final String requestUrl = "/api/admin/football/cache/leagues";
        final long leagueId = leagueIdRequest.leagueId();
        log.info("cache league :: leagueId={}", leagueId);
        return ResponseEntity.ok().body(
                adminFootballCacheWebService.cacheLeague(leagueId, requestUrl)
        );
    }

    @PostMapping("/leagues/current")
    public ResponseEntity<ApiResponse<Void>> cacheAllCurrentLeagues() {
        final String requestUrl = "/api/admin/football/cache/leagues/current";
        log.info("cache all current leagues");
        return ResponseEntity.ok().body(
                adminFootballCacheWebService.cacheAllCurrentLeagues(requestUrl)
        );
    }

    /**
     * 팀을 캐싱하고, 해당 팀이 속한 모든 현재 리그를 캐싱합니다.
     *
     * @param teamIdRequest 팀 아이디
     * @return 캐싱 결과
     */
    @PostMapping("/leagues/current/team")
    public ResponseEntity<ApiResponse<Void>> cacheTeamAndCurrentLeagues(@RequestBody TeamIdRequest teamIdRequest) {
        final String requestUrl = "/api/admin/football/cache/leagues/current/team";
        final long teamId = teamIdRequest.teamId();
        log.info("cache team and current leagues of it :: teamId={}", teamId);
        return ResponseEntity.ok().body(
                adminFootballCacheWebService.cacheTeamAndCurrentLeagues(teamId, requestUrl)
        );
    }

    /**
     * 팀의 정보만 캐싱합니다.
     *
     * @param teamIdRequest 팀 아이디
     * @return 캐싱 결과
     */
    @PostMapping("/teams")
    public ResponseEntity<ApiResponse<Void>> cacheTeam(@RequestBody TeamIdRequest teamIdRequest) {
        final String requestUrl = "/api/admin/football/cache/teams";
        final long teamId = teamIdRequest.teamId();
        log.info("cache team :: teamId={}", teamId);
        return ResponseEntity.ok().body(
                adminFootballCacheWebService.cacheTeam(teamId, requestUrl)
        );
    }

    @PostMapping("/teams/league")
    public ResponseEntity<ApiResponse<Void>> cacheTeamsByLeagueId(@RequestBody LeagueIdRequest leagueIdRequest) {
        final String requestUrl = "/api/admin/football/cache/teams/league";
        final long leagueId = leagueIdRequest.leagueId();
        log.info("cache teams by leagueId = {}", leagueId);
        return ResponseEntity.ok().body(
                adminFootballCacheWebService.cacheTeamsByLeagueId(leagueId, requestUrl)
        );
    }

    @PostMapping("/teams/squad")
    public ResponseEntity<ApiResponse<Void>> cacheSquad(@RequestBody TeamIdRequest teamIdRequest) {
        final String requestUrl = "/api/admin/football/cache/teams/squad";
        final long teamId = teamIdRequest.teamId();
        log.info("cache squad of teamId = {}", teamId);
        return ResponseEntity.ok().body(
                adminFootballCacheWebService.cacheSquad(teamId, requestUrl)
        );
    }

    /**
     * 선수 한 명을 캐싱합니다. 팀 아이디가 같이 주어진다면 연관관계 정보도 함께 캐싱합니다.
     * @param request 선수 아이디와 팀 아이디 배열. 팀 아이디가 없다면 빈 배열로 처리하며 연관관계 정보는 캐싱하지 않습니다.
     * @return
     */
    @PostMapping("/players")
    public ResponseEntity<ApiResponse<Void>> cachePlayer(@RequestBody CachePlayerSingleRequest request) {
        final String requestUrl = "/api/admin/football/cache/player";
        Long playerId = request.playerId();
        Long leagueId = request.leagueId();
        Integer season = request.season();
        log.info("cache player :: playerId={}, leagueId={}, season={}", playerId, leagueId, season);
        return ResponseEntity.ok().body(
                adminFootballCacheWebService.cachePlayerSingle(playerId, leagueId, season, requestUrl)
        );
    }

    @PostMapping("/players/prevent")
    public ResponseEntity<ApiResponse<Void>> setPreventUnlink(@RequestBody PreventUnlinkRequest request) {
        final String requestUrl = "/api/admin/football/cache/players/prevent";
        Long playerId = request.playerId();
        Boolean preventUnlink = request.preventUnlink();
        log.info("set prevent unlink :: playerId={}, preventUnlink={}", playerId, preventUnlink);
        return ResponseEntity.ok().body(
                adminFootballCacheWebService.setPlayerPreventUnlink(playerId, preventUnlink, requestUrl)
        );
    }

    /**
     * 리그 아이디와 시즌을 받아서 해당 리그의 시즌 정보를 캐싱합니다.
     */
    @PostMapping("/fixtures/league")
    public ResponseEntity<ApiResponse<Void>> cacheFixturesOfLeagueCurrentSeason(
            @RequestBody LeagueIdRequest leagueIdRequest
    ) {
        final String requestUrl = "/api/admin/football/cache/fixtures/league";
        final long leagueId = leagueIdRequest.leagueId();
        log.info("cache fixtures of leagueId = {}", leagueId);
        return ResponseEntity.ok().body(
                adminFootballCacheWebService.cacheFixturesOfLeagueCurrentSeason(leagueId, requestUrl)
        );
    }

    // ---------
    @PostMapping("/fixtures/date")
    public ResponseEntity<String> cacheFixtures(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        log.info("From : {}", from);
        log.info("To : {}", to);

        return ResponseEntity.ok().body("(Not Implemented) _Fixtures from " + from + " to " + to + " cached successfully.");
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e, HttpServletRequest request) {
        String requestURI = request.getRequestURI();
        log.warn("error occurred in URI:{}", requestURI);
        ApiResponse<Void> failureResponse = apiV1CommonResponseService.createFailureResponse(
                "요청 처리 중 오류가 발생했습니다.",
                requestURI
        );
        String requestId = failureResponse.metaData().requestId();
        log.error("error while processing request : requestId={}", requestId, e);
        log.error("error meta data : {}", failureResponse.metaData());
        return ResponseEntity.badRequest().body(
                failureResponse
        );
    }
}
