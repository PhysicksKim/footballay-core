package com.gyechunsik.scoreboard.web.admin.football.controller;

import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.service.FootballDataService;
import com.gyechunsik.scoreboard.domain.football.service.FootballExcelService;
import com.gyechunsik.scoreboard.web.admin.football.request.FixtureIdRequest;
import com.gyechunsik.scoreboard.web.admin.football.request.LeagueIdRequest;
import com.gyechunsik.scoreboard.web.admin.football.response.*;
import com.gyechunsik.scoreboard.web.admin.football.service.AdminFootballDataWebService;
import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import com.gyechunsik.scoreboard.web.common.service.ApiV1CommonResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/football")
@PreAuthorize("hasRole('ADMIN')")
public class AdminFootballDataRestController {

    private final AdminFootballDataWebService adminFootballDataWebService;
    private final FootballExcelService excelService;
    private final FootballDataService footballDataService;
    private final ApiV1CommonResponseService apiV1CommonResponseService;

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
        final String requestUrl = "/api/admin/football/leagues/available";
        ApiResponse<AvailableLeagueDto> response = adminFootballDataWebService.addAvailableLeague(leagueId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/leagues/available")
    public ResponseEntity<ApiResponse<String>> deleteAvailableLeague(@RequestParam long leagueId) {
        final String requestUrl = "/api/admin/football/leagues/available";
        ApiResponse<String> response = adminFootballDataWebService.deleteAvailableLeague(leagueId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/fixtures/available")
    public ResponseEntity<ApiResponse<AvailableFixtureDto>> getAvailableFixtures(
            @RequestParam long leagueId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        final String requestUrl = "/api/admin/football/fixtures/available";
        ZonedDateTime zonedDateTime = date == null ? ZonedDateTime.now() : date.atStartOfDay(ZoneId.of("Asia/Seoul"));
        ApiResponse<AvailableFixtureDto> response = adminFootballDataWebService.getAvailableFixtures(leagueId, zonedDateTime, requestUrl);
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
        final String requestUrl = "/api/admin/football/fixtures/available";
        ApiResponse<String> response = adminFootballDataWebService.deleteAvailableFixture(fixtureId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    /**
     * league, team, player, fixtures 조회
     */
    @GetMapping("/teams")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeamsOfLeague(long leagueId) {
        final String requestUrl = "/api/admin/football/teams";
        ApiResponse<TeamResponse> response = adminFootballDataWebService.getTeamsOfLeague(leagueId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/teams/squad")
    public ResponseEntity<ApiResponse<PlayerResponse>> getSquadOfTeam(long teamId) {
        final String requestUrl = "/api/admin/football/teams/squad";
        ApiResponse<PlayerResponse> response = adminFootballDataWebService.getSquadOfTeam(teamId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/players")
    public ResponseEntity<ApiResponse<PlayerResponse>> getPlayerInfo(long playerId) {
        final String requestUrl = "/api/admin/football/players";
        ApiResponse<PlayerResponse> response = adminFootballDataWebService.getPlayerInfo(playerId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/players/teams")
    public ResponseEntity<ApiResponse<TeamsOfPlayerResponse>> getPlayerRelations(long playerId) {
        final String requestUrl = "/api/admin/football/players/teams";
        ApiResponse<TeamsOfPlayerResponse> response = adminFootballDataWebService.getTeamsOfPlayer(playerId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/fixtures")
    public ResponseEntity<ApiResponse<FixtureResponse>> getFixturesInfo(
            long leagueId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String requestUrl = "/api/admin/football/fixtures";
        ZonedDateTime zonedDateTime = date == null ? ZonedDateTime.now() : date.atStartOfDay(ZoneId.of("Asia/Seoul"));
        ApiResponse<FixtureResponse> response = adminFootballDataWebService.getFixturesFromDate(leagueId, zonedDateTime, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/players/export/{teamId}")
    public ResponseEntity<InputStreamResource> exportPlayersToExcel(@PathVariable long teamId) throws IOException {
        List<Player> players = footballDataService.getSquadOfTeam(teamId);
        log.info("controller :: players : {}", players);
        ByteArrayInputStream in = excelService.createPlayerExcel(players);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=players.xlsx");

        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(in));
    }

    @PostMapping("/players/import")
    public ResponseEntity<?> importPlayersFromExcel(@RequestParam("file") MultipartFile file) {
        try {
            excelService.updatePlayerDetails(file);
            return ResponseEntity.ok("Players updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error processing file: " + e.getMessage());
        }
    }

    @PostMapping("/players/{playerId}/teams/{teamId}")
    public ResponseEntity<ApiResponse<Void>> addTeamPlayerRelation(
            @PathVariable(name = "playerId") final long playerId,
            @PathVariable(name = "teamId") final long teamId
    ) {
        final String requestUrl = "/api/admin/football/players/" + playerId + "/teams/" + teamId;
        ApiResponse<Void> response = adminFootballDataWebService.addTeamPlayerRelation(teamId, playerId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/players/{playerId}/teams/{teamId}")
    public ResponseEntity<ApiResponse<Void>> removeTeamPlayerRelation(
            @PathVariable(name = "playerId") final long playerId,
            @PathVariable(name = "teamId") final long teamId
    ) {
        final String requestUrl = "/api/admin/football/players/" + playerId + "/teams/" + teamId;
        ApiResponse<Void> response = adminFootballDataWebService.removeTeamPlayerRelation(teamId, playerId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

}
