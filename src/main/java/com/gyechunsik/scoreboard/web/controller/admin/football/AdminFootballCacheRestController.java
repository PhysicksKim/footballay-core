package com.gyechunsik.scoreboard.web.controller.admin.football;

import com.gyechunsik.scoreboard.domain.football.external.FootballApiCacheService;
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

    /**
     * 1. 리그 : 단일 리그 - leagueId
     * 2. 리그 : 모든 current 리그 - .
     * 3. 팀 : 단일 팀 캐싱. 팀이 속한 모든 current league 캐싱 - teamId
     * 4. 팀 : 리그에 속한 팀 - leagueId
     * 5. 선수 : 팀의 선수단 캐싱 - teamId
     * 6. 일정 : 리그의 일정 캐싱 - leagueId
     */

    @PostMapping("/league")
    public ResponseEntity<String> cacheLeague(@RequestParam("leagueId") Long leagueId) {
        footballApiCacheService.cacheLeague(leagueId);
        log.info("api league {} cached", leagueId);
        return ResponseEntity.ok().body("cache 성공 league :: EPL, UEFA Champions");
    }

    @PostMapping("/league/current")
    public ResponseEntity<String> cacheAllCurrentLeagues() {
        footballApiCacheService.cacheAllCurrentLeagues();
        log.info("api All Current Leagues Cached");
        return ResponseEntity.ok().body("cache 성공 All Current League");
    }

    @PostMapping("/team")
    public ResponseEntity<String> cacheTeam(@RequestParam("teamId") Long teamId) {
        footballApiCacheService.cacheTeamAndCurrentLeagues(teamId);
        log.info("teamId {} cached. Team and CurrentLeagues of the team", teamId);
        return ResponseEntity.ok().body("cache 성공 team :: Mancity, Manutd");
    }

    @PostMapping("/team/league")
    public ResponseEntity<String> cacheTeamsByLeagueId(@RequestParam("leagueId") Long leagueId) {
        footballApiCacheService.cacheTeamsOfLeague(leagueId);
        log.info("api teams cached of leagueId {}", leagueId);
        return ResponseEntity.ok().body("cache 성공 team :: teams by leagueId");
    }

    @PostMapping("/team/leagues")
    public ResponseEntity<String> cacheTeamAndCurrentLeagues(@RequestParam("teamId") Long teamId) {
        footballApiCacheService.cacheTeamAndCurrentLeagues(teamId);
        log.info("api manutd current seasons cached");
        return ResponseEntity.ok().body("cache 성공 current league seasons of team :: Manutd, Mancity");
    }

    @PostMapping("/team/squad")
    public ResponseEntity<String> cacheSquad(@RequestParam("teamId") Long teamId) {
        footballApiCacheService.cacheTeamSquad(teamId);
        log.info("teamId {} squad cached", teamId);
        return ResponseEntity.ok().body("cache 성공 league :: Mancity, Manutd");
    }

    /**
     * 리그 아이디와 시즌을 받아서 해당 리그의 시즌 정보를 캐싱합니다.
     */
    @PostMapping("/fixtures/league")
    public ResponseEntity<String> cacheFixturesByLeagueAndSeason(
            @RequestParam("leagueId") Long leagueId
    ) {
        log.info("leagueId : {}", leagueId);

        footballApiCacheService.cacheFixturesOfLeague(leagueId);
        log.info("Fixtures of league {} cached successfully.", leagueId);
        return ResponseEntity.ok("Fixtures of league " + leagueId + " cached successfully.");
    }

    // ---------
    @PostMapping("/fixtures")
    public ResponseEntity<String> cacheFixtures(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        log.info("From : {}", from);
        log.info("To : {}", to);

        return ResponseEntity.ok("(Not Implemented) Fixtures from " + from + " to " + to + " cached successfully.");
    }
}
