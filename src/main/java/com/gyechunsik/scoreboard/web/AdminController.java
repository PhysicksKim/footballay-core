package com.gyechunsik.scoreboard.web;

import com.gyechunsik.scoreboard.domain.football.constant.LeagueId;
import com.gyechunsik.scoreboard.domain.football.data.cache.ApiCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

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
    public ResponseEntity<String> cacheLeague() {
        apiCacheService.cacheLeague(LeagueId.EPL);
        log.info("api league epl cached");
        return ResponseEntity.ok().body("cache 성공 league");
    }

    @PostMapping("/admin/cache/team")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheTeam() {
        apiCacheService.cacheSingleTeam(LeagueId.EPL);
        log.info("api league epl cached");
        return ResponseEntity.ok().body("cache 성공 league");
    }
    @PostMapping("/admin/cache/squad")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> cacheSquad() {
        apiCacheService.cacheSquad(LeagueId.EPL);
        log.info("api league epl cached");
        return ResponseEntity.ok().body("cache 성공 league");
    }


}
