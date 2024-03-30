package com.gyechunsik.scoreboard.web.controller;

import com.gyechunsik.scoreboard.domain.football.data.cache.ApiCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;

@Slf4j
@RequiredArgsConstructor
@Controller
public class AdminController {

    private final ApiCacheService apiCacheService;
    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/test/template")
    public ResponseEntity<String> cloudFrontTest() {
        String path = "https://static.gyechunsik.site/scoreboard/admin/index.html";
        String html = restTemplate.getForObject(path, String.class);
        log.info("test/template called");

        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @GetMapping("/test/redirect")
    public String cloudFrontRedirect() {
        return "redirect:https://static.gyechunsik.site/scoreboard/admin/index.html";
    }

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
        log.info("api league epl cached");
        return ResponseEntity.ok().body("cache 성공 league :: EPL, UEFA Champions");
    }

    @PostMapping("/admin/cache/team/league")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheTeamsByLeagueId(@RequestParam("leagueId") Long leagueId) {
        apiCacheService.cacheTeams(leagueId);
        log.info("api teams cached");
        return ResponseEntity.ok().body("cache 성공 team :: teams by leagueId");
    }

    @PostMapping("/admin/cache/team")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheTeam(@RequestParam("teamId") Long teamId) {
        apiCacheService.cacheTeamAndCurrentLeagues(teamId);
        log.info("api league epl cached");
        return ResponseEntity.ok().body("cache 성공 team :: Mancity, Manutd");
    }

    @PostMapping("/admin/cache/team/squad")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheSquad(@RequestParam("teamId") Long teamId) {
        apiCacheService.cacheTeamSquad(teamId);
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

    @PostMapping("/admin/cache/league/current")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheAllCurrentLeagues() {
        apiCacheService.cacheAllCurrentLeagues();
        log.info("api All Current Leagues Cached");
        return ResponseEntity.ok().body("cache 성공 All Current League");
    }

    @PostMapping("/admin/cache/fixtures")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheFixtures(
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        // 여기에서 from과 to 날짜를 사용하여 작업을 수행합니다.
        // 예: Fixtures 데이터를 캐싱하는 로직
        log.info("From : {}", from);
        log.info("To : {}", to);

        // 처리 후 적절한 응답 반환
        return ResponseEntity.ok("Fixtures from " + from + " to " + to + " cached successfully.");
    }

    @GetMapping("/admin/league/available")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> availableLeagues() {
        return ResponseEntity.ok("This is available leagues response for test");
    }

}
