package com.gyechunsik.scoreboard.web.football.controller;

import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import com.gyechunsik.scoreboard.web.football.request.FixtureOfLeagueRequest;
import com.gyechunsik.scoreboard.web.football.response.FixtureOfLeagueResponse;
import com.gyechunsik.scoreboard.web.football.response.LeagueResponse;
import com.gyechunsik.scoreboard.web.football.service.FootballStreamWebService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * Rest API 로 football stream 데이터 제공.
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/football/stream")
public class FootballStreamDataController {

    private final FootballStreamWebService footballStreamWebService;

    @GetMapping("/leagues/available")
    public ApiResponse<LeagueResponse> leagueList() {
        final String requestUrl = "/api/football/stream/leagues/available";
        return footballStreamWebService.getLeagueList(requestUrl);
    }

    @GetMapping("/fixtures/available")
    public ApiResponse<FixtureOfLeagueResponse> fixturesOfLeague(@ModelAttribute FixtureOfLeagueRequest request) {
        final String requestUrl = "/api/football/stream/fixtures/available";
        return footballStreamWebService.getFixturesOfLeague(requestUrl, request);
    }

    @GetMapping("/fixtures")
    public ApiResponse<FixtureOfLeagueResponse> fixturesOfLeague(@RequestParam long fixtureId) {
        final String requestUrl = "/api/football/stream/fixtures";
        return null;
    }

}
