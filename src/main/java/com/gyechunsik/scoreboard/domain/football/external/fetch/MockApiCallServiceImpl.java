package com.gyechunsik.scoreboard.domain.football.external.fetch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyechunsik.scoreboard.domain.football.external.fetch.response.*;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@Profile("mockapi")
@Service
public class MockApiCallServiceImpl
        implements ApiCallService
{

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public LeagueInfoResponse leagueInfo(long leagueId) {
        String resourcePath = resolvePathOfLeagueInfo(leagueId);
        String rawString = readFile(resourcePath);
        try {
            return objectMapper.readValue(rawString, LeagueInfoResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Mock data parsing error : " + resourcePath, e);
        }
    }

    @Override
    public LeagueInfoResponse teamCurrentLeaguesInfo(long teamId) {
        String resourcePath = resolvePathOfTeamLeaguesInfo(teamId);
        String rawString = readFile(resourcePath);
        try {
            return objectMapper.readValue(rawString, LeagueInfoResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Mock data parsing error : " + resourcePath, e);
        }
    }

    @Override
    public TeamInfoResponse teamsInfo(long leagueId, int currentSeason) {
        String resourcePath = resolvePathOfTeamsByLeague(leagueId, currentSeason);
        String rawString = readFile(resourcePath);
        try {
            return objectMapper.readValue(rawString, TeamInfoResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Mock data parsing error : " + resourcePath, e);
        }
    }

    @Override
    public TeamInfoResponse teamInfo(long teamId) {
        String resourcePath = resolvePathOfTeamInfo(teamId);
        String rawString = readFile(resourcePath);
        try {
            return objectMapper.readValue(rawString, TeamInfoResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Mock data parsing error : " + resourcePath, e);
        }
    }

    @Override
    public PlayerSquadResponse playerSquad(long teamId) {
        String resourcePath = resolvePathOfPlayerSquad(teamId);
        String rawString = readFile(resourcePath);
        try {
            return objectMapper.readValue(rawString, PlayerSquadResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Mock data parsing error : " + resourcePath, e);
        }
    }

    @Override
    public LeagueInfoResponse allLeagueCurrent() {
        String resourcePath = resolvePathOfAllLeagueCurrent();
        String rawString = readFile(resourcePath);
        try {
            return objectMapper.readValue(rawString, LeagueInfoResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Mock data parsing error : " + resourcePath, e);
        }
    }

    @Override
    public FixtureResponse fixturesOfLeagueSeason(long leagueId, int season) {
        String resourcePath = resolvePathOfFixturesOfLeagueSeason(leagueId, season);
        String rawString = readFile(resourcePath);
        try {
            return objectMapper.readValue(rawString, FixtureResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Mock data parsing error : " + resourcePath, e);
        }
    }

    private String readFile(String path) {
        try {
            // 클래스 로더를 통해 실제 경로에 접근
            File file = new ClassPathResource(path).getFile();
            // 파일의 모든 내용을 읽어 문자열로 반환
            return Files.readString(file.toPath());
        } catch (IOException e) {
            throw new RuntimeException("Mock data file reading error : " + path, e);
        }
    }

    private String resolvePathOfAllLeagueCurrent() {
        return getMockApiJsonFilePath("league/current/currentleagues","","");
    }

    private String resolvePathOfLeagueInfo(long leagueId) {
        return getMockApiJsonFilePath("league/", String.valueOf(leagueId), "_league");
    }

    /**
     *
     * @param teamId
     * @return "league/{teamId}_team_leagues.json"
     */
    private String resolvePathOfTeamLeaguesInfo(long teamId) {
        return getMockApiJsonFilePath("league/", String.valueOf(teamId), "_team_leagues");
    }

    /**
     *
     * @param leagueId
     * @param currentSeason
     * @return "league/{leagueId}_teams_by_league.json"
     */
    private String resolvePathOfTeamsByLeague(long leagueId, int currentSeason) {
        if(currentSeason != 2023 && leagueId != 39)
            throw new IllegalArgumentException("Mock league 는 leagueId=39,currentSeason=2023 만 가능합니다. " +
                    "주어진 leagueId=" + leagueId + ",currentSeason=" + currentSeason);
        return getMockApiJsonFilePath("league/", String.valueOf(leagueId), "_teams_by_league");
    }

    private String resolvePathOfTeamInfo(long teamId) {
        return getMockApiJsonFilePath("team/", String.valueOf(teamId), "_team");
    }

    private String resolvePathOfPlayerSquad(long teamId) {
        return getMockApiJsonFilePath("player/squad/", String.valueOf(teamId), "_squad");
    }

    private String resolvePathOfFixturesOfLeagueSeason(long leagueId, int season) {
        return getMockApiJsonFilePath("fixture/", leagueId +"_"+ season, "_fixtures_of_league_season");
    }

    /**
     * @param typePath ex. player/squad/
     * @param id
     * @return
     */
    private String getMockApiJsonFilePath(String typePath, String id, String suffix) {
        return "/dev/mockapi/" + typePath + id + suffix + ".json";
    }
}
