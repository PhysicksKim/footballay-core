package com.gyechunsik.scoreboard.web.admin.football.controller;

import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
import com.gyechunsik.scoreboard.web.admin.football.dto.LeagueDto;
import com.gyechunsik.scoreboard.web.admin.football.service.AdminFootballCacheWebService;
import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/football/cache")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFootballCacheRestController {

    private final FootballApiCacheService footballApiCacheService;
    private final AdminFootballCacheWebService adminFootballCacheWebService;

    /**
     * 1. 리그 : 단일 리그 - leagueId
     * 2. 리그 : 모든 current 리그 - .
     * 3. 팀 : 단일 팀 캐싱. 팀이 속한 모든 current league 캐싱 - teamId
     * 4. 팀 : 리그에 속한 팀 - leagueId
     * 5. 선수 : 팀의 선수단 캐싱 - teamId
     * 6. 일정 : 리그의 일정 캐싱 - leagueId
     */

    @PostMapping("/leagues")
    public ResponseEntity<ApiResponse<Void>> cacheLeague(@RequestParam("leagueId") Long leagueId) {
        log.info("cache league :: leagueId={}", leagueId);
        return ResponseEntity.ok().body(
                adminFootballCacheWebService.cacheLeague(leagueId, "/api/admin/football/cache/leagues")
        );
    }

    @PostMapping("/leagues/current")
    public ResponseEntity<ApiResponse<Void>> cacheAllCurrentLeagues() {
        log.info("cache all current leagues");
        return ResponseEntity.ok().body(
                adminFootballCacheWebService.cacheAllCurrentLeagues("/api/admin/football/cache/leagues/current")
        );
    }

    /**
     * 팀을 캐싱하고, 해당 팀이 속한 모든 현재 리그를 캐싱합니다.
     * @param teamId 팀 아이디
     * @return 캐싱 결과
     */
    @PostMapping("/leagues/current/team")
    public ResponseEntity<ApiResponse<Void>> cacheTeamAndCurrentLeagues(@RequestParam("teamId") Long teamId) {
        log.info("cache team and current leagues of it :: teamId={}", teamId);
        return ResponseEntity.ok().body(
                adminFootballCacheWebService.cacheTeamAndCurrentLeagues(teamId, "/api/admin/football/cache/leagues/current/team")
        );
    }

    /**
     * 팀의 정보만 캐싱합니다.
     * @param teamId 팀 아이디
     * @return 캐싱 결과
     */
    @PostMapping("/teams")
    public ResponseEntity<ApiResponse<Void>> cacheTeam(@RequestParam("teamId") Long teamId) {
        log.info("cache team :: teamId={}", teamId);
        return ResponseEntity.ok().body(
                adminFootballCacheWebService.cacheTeam(teamId, "/api/admin/football/cache/teams")
        );
    }

    @PostMapping("/teams/league")
    public ResponseEntity<ApiResponse<Void>> cacheTeamsByLeagueId(@RequestParam("leagueId") Long leagueId) {
        log.info("cache teams by leagueId = {}", leagueId);
        return ResponseEntity.ok().body(
                adminFootballCacheWebService.cacheTeamsByLeagueId(leagueId, "/api/admin/football/cache/teams/league")
        );
    }

    @PostMapping("/teams/squad")
    public ResponseEntity<ApiResponse<Void>> cacheSquad(@RequestParam("teamId") Long teamId) {
        log.info("cache squad of teamId = {}", teamId);
        return ResponseEntity.ok().body(
                adminFootballCacheWebService.cacheSquad(teamId, "/api/admin/football/cache/teams/squad")
        );
    }

    /**
     * 리그 아이디와 시즌을 받아서 해당 리그의 시즌 정보를 캐싱합니다.
     */
    @PostMapping("/fixtures/league")
    public ResponseEntity<ApiResponse<Void>> cacheFixturesOfLeagueCurrentSeason(
            @RequestParam("leagueId") Long leagueId
    ) {
        log.info("cache fixtures of leagueId = {}", leagueId);
        return ResponseEntity.ok().body(
                adminFootballCacheWebService.cacheFixturesOfLeagueCurrentSeason(leagueId, "/api/admin/football/cache/fixtures/league")
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

        return ResponseEntity.ok().body("(Not Implemented) Fixtures from " + from + " to " + to + " cached successfully.");
    }
}
