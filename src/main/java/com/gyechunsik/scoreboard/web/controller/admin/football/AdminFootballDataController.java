package com.gyechunsik.scoreboard.web.controller.admin.football;

import com.gyechunsik.scoreboard.domain.football.FootballRoot;
import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.service.FootballAvailableRefacService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/football/available")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFootballDataController {

    /**
     * - Available Leagues 조회
     * GET : 가능 리그 조회
     * POST : 가능 리그 추가
     * DELETE : 가능 리그 삭제
     * - Available Fixtures 조회
     * GET : 가능 팀 조회
     * POST : 가능 팀 추가
     * DELETE : 가능 팀 삭제
     */

    private final FootballRoot footballRoot;

    @GetMapping("/leagues")
    public ResponseEntity<List<League>> getAvailableLeagues() {
        List<League> leagues = footballRoot.getAvailableLeagues();
        return ResponseEntity.ok().body(leagues);
    }

    @PostMapping("/leagues")
    public ResponseEntity<String> addAvailableLeague(long leagueId) {
        League league;
        try {
            league = footballRoot.addAvailableLeague(leagueId);
        } catch (Exception e) {
            log.error("error while adding available league :: {}", e.getMessage());
            return ResponseEntity.badRequest().body("리그 추가 실패");
        }
        return ResponseEntity.ok().body("리그 추가 성공");
    }

    @DeleteMapping("/leagues")
    public ResponseEntity<String> deleteAvailableLeague(long leagueId) {
        boolean isSuccess = footballRoot.removeAvailableLeague(leagueId);
        if(!isSuccess) {
            return ResponseEntity.badRequest().body("리그 삭제 실패");
        }
        return ResponseEntity.ok().body("리그 삭제 성공");
    }

    @GetMapping("/fixtures")
    public ResponseEntity<List<Fixture>> getAvailableFixtures(long leagueId, ZonedDateTime date) {
        List<Fixture> availableFixtures = footballRoot.getAvailableFixtures(leagueId, date);
        return ResponseEntity.ok().body(availableFixtures);
    }

    @PostMapping("/fixtures")
    public ResponseEntity<String> addAvailableFixture(long fixtureId) {
        Fixture fixture;
        try {
            fixture = footballRoot.addAvailableFixture(fixtureId);
        } catch (Exception e) {
            log.error("error while adding available fixture :: {}", e.getMessage());
            return ResponseEntity.badRequest().body("경기 추가 실패");
        }
        return ResponseEntity.ok().body("경기 추가 성공");
    }

    @DeleteMapping("/fixtures")
    public ResponseEntity<String> deleteAvailableFixture(long fixtureId) {
        boolean isSuccess = footballRoot.removeAvailableFixture(fixtureId);
        if(!isSuccess) {
            return ResponseEntity.badRequest().body("경기 삭제 실패");
        }
        return ResponseEntity.ok().body("경기 삭제 성공");
    }

}
