package com.gyechunsik.scoreboard.domain.football.data.fetch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.LeagueInfoResponse;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.LeagueTeamsInfoResponse;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.PlayerSquadResponse;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.TeamInfoResponse;
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
    public TeamInfoResponse teamInfo(long teamId) {
        String resourcePath = resolvePathOfLTeamInfo(teamId);
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

    private String resolvePathOfLeagueInfo(long leagueId) {
        return getMockApiJsonFilePath("league/", String.valueOf(leagueId), "_league");
    }

    private String resolvePathOfTeamLeaguesInfo(long teamId) {
        return getMockApiJsonFilePath("league/", String.valueOf(teamId), "_team_leagues");
    }

    private String resolvePathOfLTeamInfo(long teamId) {
        return getMockApiJsonFilePath("team/", String.valueOf(teamId), "_team");
    }

    private String resolvePathOfPlayerSquad(long teamId) {
        return getMockApiJsonFilePath("player/squad/", String.valueOf(teamId), "_squad");
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
