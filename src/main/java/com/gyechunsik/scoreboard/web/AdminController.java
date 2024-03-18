package com.gyechunsik.scoreboard.web;

import com.gyechunsik.scoreboard.domain.football.data.cache.ApiCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@RequiredArgsConstructor
@Controller
public class AdminController {

    private final ApiCacheService apiCacheService;

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminIndexPage(Authentication authentication) {
        log.info("auth details : {}", authentication.getDetails());
        log.info("auth isAuth : {}", authentication.isAuthenticated());
        log.info("auth role : {}", authentication.getAuthorities());
        log.info("auth toString : {}", authentication);
        return "admin/adminindex";
    }

    @PostMapping("/admin/cache/league")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheLeague(@RequestParam("leagueId") Long leagueId) {
        apiCacheService.cacheLeague(leagueId);
        // apiCacheService.cacheLeague(LeagueId.UEFA_CHAMPIONS);
        log.info("api league epl cached");
        return ResponseEntity.ok().body("cache 성공 league :: EPL, UEFA Champions");
    }

    @PostMapping("/admin/cache/team")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheTeam(@RequestParam("teamId") Long teamId) {
        apiCacheService.cacheSingleTeam(teamId);
        // apiCacheService.cacheSingleTeam(TeamId.MANUTD);
        log.info("api league epl cached");
        return ResponseEntity.ok().body("cache 성공 team :: Mancity, Manutd");
    }

    @PostMapping("/admin/cache/team/squad")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheSquad(@RequestParam("teamId") Long teamId) {
        apiCacheService.cacheTeamSquad(teamId);
        // apiCacheService.cacheTeamSquad(TeamId.MANUTD);
        log.info("api league epl cached");
        return ResponseEntity.ok().body("cache 성공 league :: Mancity, Manutd");
    }

    @PostMapping("/admin/cache/team/leagues")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheTeamAndCurrentLeagues(@RequestParam("teamId") Long teamId) {
        apiCacheService.cacheTeamAndCurrentLeagues(teamId);
        log.info("api manutd current seasons cached");
        return ResponseEntity.ok().body("cache 성공 current league seasons of team :: Manutd, Mancity");
    }

    @PostMapping("/admin/cache/team/leagues")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheAllCurrentLeagues() {
        apiCacheService.cacheAllCurrentLeagues();
        log.info("api manutd current seasons cached");
        return ResponseEntity.ok().body("cache 성공 current league seasons of team :: Manutd, Mancity");
    }



}
