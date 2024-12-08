package com.gyechunsik.scoreboard.domain.football.scheduler.live;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.service.FootballDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Component
public class CheckPostJobDeleteImpl implements CheckPostJobDelete {

    private static final int HOURS_AFTER_MATCH_FINISHED = 6;

    private final FootballDataService footballDataService;

    @Override
    public boolean isLongAfterMatchFinished(long fixtureId) {
        Fixture fixtureById = footballDataService.getFixtureById(fixtureId);
        LocalDateTime kickoffTime = fixtureById.getDate();
        LocalDateTime now = LocalDateTime.now();
        boolean after = now.isAfter(kickoffTime.plusHours(HOURS_AFTER_MATCH_FINISHED));
        if(after) {
            log.info("Fixture {} is long after match finished. now={}, kickoffTime={}", fixtureId, now, kickoffTime);
        }
        return after;
    }
}
