package com.gyechunsik.scoreboard.web.controller.admin;

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
@RequestMapping("/api/admin/cache")
public class AdminCacheRestController {

    private final FootballApiCacheService footballApiCacheService;

    @PostMapping("/league")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheLeague(@RequestParam("leagueId") Long leagueId) {
        footballApiCacheService.cacheLeague(leagueId);
        log.info("api league {} cached", leagueId);
        return ResponseEntity.ok().body("cache 성공 league :: EPL, UEFA Champions");
    }

    @PostMapping("/team/league")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheTeamsByLeagueId(@RequestParam("leagueId") Long leagueId) {
        footballApiCacheService.cacheTeamsOfLeague(leagueId);
        log.info("api teams cached of leagueId {}", leagueId);
        return ResponseEntity.ok().body("cache 성공 team :: teams by leagueId");
    }

    @PostMapping("/team")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheTeam(@RequestParam("teamId") Long teamId) {
        footballApiCacheService.cacheTeamAndCurrentLeagues(teamId);
        log.info("teamId {} cached. Team and CurrentLeagues of the team", teamId);
        return ResponseEntity.ok().body("cache 성공 team :: Mancity, Manutd");
    }

    @PostMapping("/team/squad")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheSquad(@RequestParam("teamId") Long teamId) {
        footballApiCacheService.cacheTeamSquad(teamId);
        log.info("teamId {} squad cached", teamId);
        return ResponseEntity.ok().body("cache 성공 league :: Mancity, Manutd");
    }

    @PostMapping("/team/leagues")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheTeamAndCurrentLeagues(@RequestParam("teamId") Long teamId) {
        footballApiCacheService.cacheTeamAndCurrentLeagues(teamId);
        log.info("api manutd current seasons cached");
        return ResponseEntity.ok().body("cache 성공 current league seasons of team :: Manutd, Mancity");
    }

    @PostMapping("/league/current")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheAllCurrentLeagues() {
        footballApiCacheService.cacheAllCurrentLeagues();
        log.info("api All Current Leagues Cached");
        return ResponseEntity.ok().body("cache 성공 All Current League");
    }

    @PostMapping("/fixtures")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheFixtures(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        log.info("From : {}", from);
        log.info("To : {}", to);

        return ResponseEntity.ok("(Not Implemented) Fixtures from " + from + " to " + to + " cached successfully.");
    }

    /**
     * 리그 아이디와 시즌을 받아서 해당 리그의 시즌 정보를 캐싱합니다.
     */
    @PostMapping("/fixtures/league")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheFixturesByLeagueAndSeason(
            @RequestParam("leagueId") Long leagueId,
            @RequestParam("season") Integer season
    ) {
        log.info("leagueId : {}", leagueId);
        log.info("season : {}", season);

        footballApiCacheService.cacheFixturesOfLeagueSeason(leagueId, season);
        log.info("Fixtures of league {} in season {} cached successfully.", leagueId, season);
        return ResponseEntity.ok("Fixtures of league " + leagueId + " in season " + season + " cached successfully.");
    }

}
