package com.gyechunsik.scoreboard.websocket.temp;

import com.gyechunsik.scoreboard.websocket.service.ScoreboardWebsocketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.security.SecureRandom;

@Slf4j
@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class TestWSMessageSender {

    private final ScoreboardWebsocketService service;

    // 10초에 한 번씩 모든 세션에 score 메시지를 보냅니다.
    @Scheduled(fixedDelay = 5000)
    public void sendScoreMessage() {
        int teamAScore = new SecureRandom().nextInt(9);
        int teamBScore = new SecureRandom().nextInt(9);

        // service.sendScoreMessage(teamAScore, teamBScore);
        // log.info("Score Message Sent :: " + teamAScore + " : " + teamBScore);
    }
}
