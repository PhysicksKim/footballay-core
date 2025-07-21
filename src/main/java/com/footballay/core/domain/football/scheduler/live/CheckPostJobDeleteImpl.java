package com.footballay.core.domain.football.scheduler.live;

import com.footballay.core.domain.football.persistence.Fixture;
import com.footballay.core.domain.football.service.FootballDataService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Transactional
@Component
public class CheckPostJobDeleteImpl implements CheckPostJobDelete {
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(CheckPostJobDeleteImpl.class);
    private static final int HOURS_AFTER_MATCH_FINISHED = 6;
    private final FootballDataService footballDataService;

    @Override
    public boolean isLongAfterMatchFinished(long fixtureId) {
        Fixture fixtureById = footballDataService.getFixtureById(fixtureId);
        LocalDateTime kickoffTime = fixtureById.getDate();
        LocalDateTime now = LocalDateTime.now();
        boolean after = now.isAfter(kickoffTime.plusHours(HOURS_AFTER_MATCH_FINISHED));
        if (after) {
            log.info("Fixture {} is long after match finished. now={}, kickoffTime={}", fixtureId, now, kickoffTime);
        }
        return after;
    }

    public CheckPostJobDeleteImpl(final FootballDataService footballDataService) {
        this.footballDataService = footballDataService;
    }
}
