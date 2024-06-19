package com.gyechunsik.scoreboard.web.admin.football.controller;

import com.gyechunsik.scoreboard.domain.football.FootballRoot;
import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.web.admin.football.dto.AvailableLeagueDto;
import com.gyechunsik.scoreboard.web.admin.football.service.AdminFootballDataWebService;
import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
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
public class AdminFootballDataRestController {

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
    private final AdminFootballDataWebService adminFootballDataWebService;

    @GetMapping("/leagues/available")
    public ResponseEntity<ApiResponse<AvailableLeagueDto>> getAvailableLeagues() {
        final String requestUrl = "/api/admin/football/available/leagues/available";
        ApiResponse<AvailableLeagueDto> availableLeagues = adminFootballDataWebService.getAvailableLeagues(
                requestUrl
        );
        return ResponseEntity.ok().body(availableLeagues);
    }

    @PostMapping("/leagues/available")
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

    @DeleteMapping("/leagues/available")
    public ResponseEntity<String> deleteAvailableLeague(long leagueId) {
        boolean isSuccess = footballRoot.removeAvailableLeague(leagueId);
        if(!isSuccess) {
            return ResponseEntity.badRequest().body("리그 삭제 실패");
        }
        return ResponseEntity.ok().body("리그 삭제 성공");
    }

    @GetMapping("/fixtures/available")
    public ResponseEntity<List<Fixture>> getAvailableFixtures(long leagueId, ZonedDateTime date) {
        List<Fixture> availableFixtures = footballRoot.getAvailableFixtures(leagueId, date);
        return ResponseEntity.ok().body(availableFixtures);
    }

    @PostMapping("/fixtures/available")
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

    @DeleteMapping("/fixtures/available")
    public ResponseEntity<String> deleteAvailableFixture(long fixtureId) {
        boolean isSuccess = footballRoot.removeAvailableFixture(fixtureId);
        if(!isSuccess) {
            return ResponseEntity.badRequest().body("경기 삭제 실패");
        }
        return ResponseEntity.ok().body("경기 삭제 성공");
    }

    /**
     * league, team, player, fixtures 조회
     */
    @GetMapping("/teams")
    public ResponseEntity<String> getTeamsOfLeague(long leagueId) {
        return ResponseEntity.ok().body("팀 조회 성공");
    }

    @GetMapping("/teams/squad")
    public ResponseEntity<String> getSquadOfTeam(long teamId) {
        return ResponseEntity.ok().body("선수 조회 성공");
    }

    @GetMapping("/players")
    public ResponseEntity<String> getPlayerInfo(long playerId) {
        return ResponseEntity.ok().body("선수 조회 성공");
    }

    @GetMapping("/fixtures")
    public ResponseEntity<String> getFixtureInfo(long fixtureId) {
        return ResponseEntity.ok().body("경기 조회 성공");
    }

}
