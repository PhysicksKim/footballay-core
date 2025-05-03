package com.footballay.core.domain.football.external;

import com.footballay.core.domain.football.external.fetch.response.StandingsResponse;
import com.footballay.core.domain.football.persistence.League;
import com.footballay.core.domain.football.persistence.Team;

import java.util.ArrayList;
import java.util.List;

public class MockStandingsResponseBuilder {

    /**
     * League와 Team varargs를 받아서 StandingsResponse를 생성합니다.
     * 각 팀은 배열 순서대로 rank 1부터 부여되며, stat 필드들은 팀 수를 기반으로 적당한 값을 생성합니다.
     *
     * @param league 테스트용 League 객체
     * @param teams  테스트용 Team 배열 (varargs)
     * @return 생성된 StandingsResponse 객체
     */
    public static StandingsResponse buildMockStandingsResponse(League league, Team... teams) {
        StandingsResponse response = new StandingsResponse();

        // 리그 정보 설정
        StandingsResponse._StandingResponseData leagueData = new StandingsResponse._StandingResponseData();
        leagueData.setId((int)(long)league.getLeagueId());
        leagueData.setName(league.getName());
        leagueData.setCountry("MockCountry");
        leagueData.setLogo("mock://league/logo/" + league.getLeagueId());
        leagueData.setFlag("mock://league/flag/" + league.getLeagueId());
        leagueData.setSeason(league.getCurrentSeason());

        // 팀별 순위 목록 생성
        List<StandingsResponse._Standing> standingsList = new ArrayList<>();
        int teamCount = teams.length;
        for (int i = 0; i < teamCount; i++) {
            Team team = teams[i];
            StandingsResponse._Standing standing = new StandingsResponse._Standing();

            // 팀 기본 정보 설정
            StandingsResponse._Team standingTeam = new StandingsResponse._Team();
            standingTeam.setId(team.getId());
            standingTeam.setName(team.getName());
            standingTeam.setLogo(team.getLogo());
            standing.setTeam(standingTeam);

            // 순위 (1부터 시작)
            standing.setRank(i + 1);

            // 포인트: 팀 수에 따라 높은 순위일수록 높은 포인트 (예: (총팀수 - index) * 3)
            standing.setPoints((teamCount - i) * 3);

            // 득실차: 예) (총팀수 - index) * 2
            standing.setGoalsDiff((teamCount - i) * 2);

            standing.setGroup("Group A");
            standing.setForm("W");
            standing.setStatus("same");
            standing.setDescription(null);

            // 모든 경기 통계 (all)
            int winAll = teamCount - i;   // 간단하게 순위에 따라 감소하는 값
            int drawAll = 2;
            int loseAll = 10 - winAll - drawAll;  // 임의의 총 경기수 10 기준
            StandingsResponse._WinDrawLose allStats = new StandingsResponse._WinDrawLose();
            allStats.setPlayed(10);
            allStats.setWin(winAll);
            allStats.setDraw(drawAll);
            allStats.setLose(loseAll);
            StandingsResponse._Goals allGoals = new StandingsResponse._Goals();
            allGoals.setForGoals(winAll * 2);
            allGoals.setAgainst(drawAll * 1);
            allStats.setGoals(allGoals);
            standing.setAll(allStats);

            // 홈 경기 통계 (예: all의 절반 정도)
            StandingsResponse._WinDrawLose homeStats = new StandingsResponse._WinDrawLose();
            homeStats.setPlayed(5);
            homeStats.setWin(Math.max(0, winAll / 2));
            homeStats.setDraw(1);
            homeStats.setLose(5 - (Math.max(0, winAll / 2) + 1));
            StandingsResponse._Goals homeGoals = new StandingsResponse._Goals();
            homeGoals.setForGoals(Math.max(0, winAll));
            homeGoals.setAgainst(1);
            homeStats.setGoals(homeGoals);
            standing.setHome(homeStats);

            // 원정 경기 통계 (예: 나머지 값)
            StandingsResponse._WinDrawLose awayStats = new StandingsResponse._WinDrawLose();
            awayStats.setPlayed(5);
            awayStats.setWin(Math.max(0, winAll - (winAll / 2)));
            awayStats.setDraw(1);
            awayStats.setLose(5 - (Math.max(0, winAll - (winAll / 2)) + 1));
            StandingsResponse._Goals awayGoals = new StandingsResponse._Goals();
            awayGoals.setForGoals(Math.max(0, winAll));
            awayGoals.setAgainst(1);
            awayStats.setGoals(awayGoals);
            standing.setAway(awayStats);

            // 업데이트 시간 (ISO-8601 형식)
            standing.setUpdate("2025-03-17T00:00:00+00:00");

            standingsList.add(standing);
        }

        // standing 필드는 List<List<_Standing>> 형태로 설정 (첫 번째 그룹 사용)
        List<List<StandingsResponse._Standing>> standings = new ArrayList<>();
        standings.add(standingsList);
        leagueData.setStandings(standings);

        // 리그 순위 응답 객체에 리그 데이터를 설정
        StandingsResponse._LeagueStandingResponse leagueStandingResponse = new StandingsResponse._LeagueStandingResponse();
        leagueStandingResponse.setLeague(leagueData);

        List<StandingsResponse._LeagueStandingResponse> responseList = new ArrayList<>();
        responseList.add(leagueStandingResponse);
        response.setResponse(responseList);

        return response;
    }
}
