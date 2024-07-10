package com.gyechunsik.scoreboard.web.football.controller;

import com.gyechunsik.scoreboard.domain.football.entity.live.FixtureEvent;
import com.gyechunsik.scoreboard.web.common.dto.ApiResponse;
import com.gyechunsik.scoreboard.web.football.request.FixtureOfLeagueRequest;
import com.gyechunsik.scoreboard.web.football.response.FixtureOfLeagueResponse;
import com.gyechunsik.scoreboard.web.football.response.LeagueResponse;
import com.gyechunsik.scoreboard.web.football.response.fixture.info.FixtureInfoResponse;
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

    /**
     * 이용 가능한 리그 목록 조회
     * @return ApiResponse<LeagueResponse> 리그 목록
     */
    @GetMapping("/leagues/available")
    public ApiResponse<LeagueResponse> leagueList() {
        final String requestUrl = "/api/football/stream/leagues/available";
        return footballStreamWebService.getLeagueList(requestUrl);
    }

    /**
     * 리그에 속한 이용 가능한 경기 일정 조회
     * @param request 리그 ID
     * @return ApiResponse<FixtureOfLeagueResponse> 리그에 속한 경기 일정
     */
    @GetMapping("/fixtures/available")
    public ApiResponse<FixtureOfLeagueResponse> fixturesOfLeague(@ModelAttribute FixtureOfLeagueRequest request) {
        final String requestUrl = "/api/football/stream/fixtures/available";
        return footballStreamWebService.getFixturesOfLeague(requestUrl, request);
    }

    /**
     * {@link FixtureInfoResponse} 라이브 상태 및 이벤트 정보를 포함하여 Fixture 정보를 제공합니다.
     * @param fixtureId 경기 ID
     * @return ApiResponse<FixtureInfoResponse> 경기 정보
     */
    @GetMapping("/fixtures")
    public ApiResponse<FixtureInfoResponse> fixturesOfLeague(@RequestParam long fixtureId) {
        final String requestUrl = "/api/football/stream/fixtures";
        return null;
    }

}
