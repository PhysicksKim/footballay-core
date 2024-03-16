package com.gyechunsik.scoreboard.domain.football.data.fetch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gyechunsik.scoreboard.domain.football.data.fetch.response.PlayerSquadResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

@Profile("mockapi")
@Service
public class MockApiCallServiceImpl
        // implements ApiCallService
{

    private final ObjectMapper objectMapper = new ObjectMapper();

    // @Override
    public PlayerSquadResponse playerSquad(long teamId) {
        // 경로 구성
        String resourcePath = "/dev/player/squad/" + teamId + "_squad.json";

        try {
            // 클래스 로더를 통해 실제 경로에 접근
            File file = new ClassPathResource(resourcePath).getFile();
            // 파일의 모든 내용을 읽어 문자열로 반환
            String rawJson = Files.readString(file.toPath());
            return objectMapper.readValue(rawJson, PlayerSquadResponse.class);
        } catch (IOException e) {
            throw new RuntimeException("Mock data file reading error for teamId: " + teamId, e);
        }
    }

    // @Override
    public String teamInfo(long teamId) throws IOException {
        return null;
    }
}
