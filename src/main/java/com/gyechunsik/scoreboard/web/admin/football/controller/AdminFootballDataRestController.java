package com.gyechunsik.scoreboard.web.admin.football.controller;

import com.gyechunsik.scoreboard.domain.football.persistence.Player;
import com.gyechunsik.scoreboard.domain.football.service.FootballDataService;
import com.gyechunsik.scoreboard.domain.football.service.FootballExcelService;
import com.gyechunsik.scoreboard.web.admin.football.response.*;
import com.gyechunsik.scoreboard.web.admin.football.service.AdminFootballDataWebService;
import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
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

    private static final String CONTROLLER_URL = "/api/admin/football";

    @GetMapping("/leagues/available")
    public ResponseEntity<ApiResponse<AvailableLeagueResponse>> getAvailableLeagues() {
        final String requestUrl = CONTROLLER_URL + "/leagues/available";
        ApiResponse<AvailableLeagueResponse> availableLeagues = adminFootballDataWebService.getAvailableLeagues(requestUrl);
        return ResponseEntity.ok().body(availableLeagues);
    }

    @PostMapping("/leagues/{leagueId}/available")
    public ResponseEntity<ApiResponse<AvailableLeagueResponse>> addAvailableLeague(@PathVariable long leagueId) {
        final String requestUrl = CONTROLLER_URL + "/leagues/" + leagueId + "/available";
        ApiResponse<AvailableLeagueResponse> response = adminFootballDataWebService.addAvailableLeague(leagueId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/leagues/{leagueId}/available")
    public ResponseEntity<ApiResponse<String>> deleteAvailableLeague(@PathVariable long leagueId) {
        final String requestUrl = CONTROLLER_URL + "/leagues/" + leagueId + "/available";
        ApiResponse<String> response = adminFootballDataWebService.deleteAvailableLeague(leagueId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/leagues/{leagueId}/fixtures")
    public ResponseEntity<ApiResponse<FixtureResponse>> getFixturesInfo(
            @PathVariable long leagueId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String requestUrl = CONTROLLER_URL + "/leagues/" + leagueId + "/fixtures";
        ZonedDateTime zonedDateTime = date == null ? ZonedDateTime.now() : date.atStartOfDay(ZoneId.of("Asia/Seoul"));
        ApiResponse<FixtureResponse> response = adminFootballDataWebService.getFixturesFromDate(leagueId, zonedDateTime, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/leagues/{leagueId}/fixtures/available")
    public ResponseEntity<ApiResponse<AvailableFixtureResponse>> getAvailableFixtures(
            @PathVariable long leagueId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        final String requestUrl = CONTROLLER_URL + "/leagues/" + leagueId + "/fixtures/available";
        ZonedDateTime zonedDateTime = date == null ? ZonedDateTime.now() : date.atStartOfDay(ZoneId.of("Asia/Seoul"));
        ApiResponse<AvailableFixtureResponse> response = adminFootballDataWebService.getAvailableFixtures(leagueId, zonedDateTime, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping("/fixtures/{fixtureId}/available")
    public ResponseEntity<ApiResponse<AvailableFixtureResponse>> addAvailableFixture(
            @PathVariable long fixtureId
    ) {
        String requestUrl = CONTROLLER_URL + "/fixtures/" + fixtureId + "/available";
        log.info("Add available fixture :: fixtureId : {}", fixtureId);
        ApiResponse<AvailableFixtureResponse> response = adminFootballDataWebService.addAvailableFixture(fixtureId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/fixtures/{fixtureId}/available")
    public ResponseEntity<ApiResponse<String>> deleteAvailableFixture(@PathVariable long fixtureId) {
        log.info("Delete available fixture :: fixtureId : {}", fixtureId);
        final String requestUrl = CONTROLLER_URL + "/fixtures/" + fixtureId + "/available";
        ApiResponse<String> response = adminFootballDataWebService.deleteAvailableFixture(fixtureId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/leagues/{leagueId}/fixtures/date")
    public ResponseEntity<ApiResponse<FixtureResponse>> getFixturesOnDate(
            @PathVariable long leagueId,
            @RequestParam(required = true) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        String requestUrl = CONTROLLER_URL + "/leagues/" + leagueId + "/fixtures/date";
        ZonedDateTime zonedDateTime = date.atStartOfDay(ZoneId.of("Asia/Seoul"));
        ApiResponse<FixtureResponse> response = adminFootballDataWebService.getFixturesOnDate(leagueId, zonedDateTime, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/leagues/{leagueId}/teams")
    public ResponseEntity<ApiResponse<TeamResponse>> getTeamsOfLeague(@PathVariable long leagueId) {
        final String requestUrl = CONTROLLER_URL + "/leagues/" + leagueId + "/teams";
        ApiResponse<TeamResponse> response = adminFootballDataWebService.getTeamsOfLeague(leagueId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/teams/{teamId}/squad")
    public ResponseEntity<ApiResponse<PlayerResponse>> getSquadOfTeam(@PathVariable long teamId) {
        final String requestUrl = CONTROLLER_URL + "/teams/" + teamId + "/squad";
        ApiResponse<PlayerResponse> response = adminFootballDataWebService.getSquadOfTeam(teamId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/players/{playerId}")
    public ResponseEntity<ApiResponse<PlayerResponse>> getPlayerInfo(@PathVariable long playerId) {
        final String requestUrl = CONTROLLER_URL + "/players/" + playerId;
        ApiResponse<PlayerResponse> response = adminFootballDataWebService.getPlayerInfo(playerId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/players/{playerId}/teams")
    public ResponseEntity<ApiResponse<TeamsOfPlayerResponse>> getPlayerRelations(@PathVariable long playerId) {
        final String requestUrl = CONTROLLER_URL + "/players/" + playerId + "/teams";
        ApiResponse<TeamsOfPlayerResponse> response = adminFootballDataWebService.getTeamsOfPlayer(playerId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/teams/{teamId}/players/export")
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
            @PathVariable long playerId,
            @PathVariable long teamId
    ) {
        final String requestUrl = CONTROLLER_URL + "/players/" + playerId + "/teams/" + teamId;
        ApiResponse<Void> response = adminFootballDataWebService.addTeamPlayerRelation(teamId, playerId, requestUrl);
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/players/{playerId}/teams/{teamId}")
    public ResponseEntity<ApiResponse<Void>> removeTeamPlayerRelation(
            @PathVariable long playerId,
            @PathVariable long teamId
    ) {
        final String requestUrl = CONTROLLER_URL + "/players/" + playerId + "/teams/" + teamId;
        ApiResponse<Void> response = adminFootballDataWebService.removeTeamPlayerRelation(teamId, playerId, requestUrl);
        return ResponseEntity.ok().body(response);
    }
}
