package com.gyechunsik.scoreboard.domain.football.service;

import com.gyechunsik.scoreboard.domain.football.persistence.Fixture;
import com.gyechunsik.scoreboard.domain.football.scheduler.lineup.StartLineupJobSchedulerService;
import com.gyechunsik.scoreboard.domain.football.scheduler.live.LiveFixtureJobSchedulerService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.zone.ZoneRulesException;

@Service
@RequiredArgsConstructor
@Slf4j
public class FixtureJobManageService {

    private final StartLineupJobSchedulerService startLineupJobSchedulerService;
    private final LiveFixtureJobSchedulerService liveFixtureJobSchedulerService;

    private final FixtureDataIntegrityService dataIntegrityService;

    private final static int LINEUP_ANNOUNCE_BEFORE_HOUR = 1;

    /**
     *
     * @param fixture Fixture 연관 데이터를 모두 사용하므로 Fetch Join 으로 load 된 Fixture 가 아니면 N+1 이 발생할 수 있습니다.
     */
    public void addFixtureJobs(@NotNull Fixture fixture) {
        long fixtureId = fixture.getFixtureId();
        try {
            dataIntegrityService.cleanUpFixtureLiveData(fixture);
            enrollFixtureJobs(fixture, fixtureId);

            log.info("Fixture jobs added for fixtureId={}", fixtureId);
            fixture.setAvailable(true);
        } catch (SchedulerException e) {
            log.error("Failed to add fixture jobs for fixtureId={}", fixtureId, e);
        }

    }

    private void enrollFixtureJobs(Fixture fixture, long fixtureId) throws SchedulerException {
        ZonedDateTime kickOffTime = toSeoulZonedDateTime(
                fixture.getDate(), fixture.getTimezone(), fixture.getTimestamp()
        );
        ZonedDateTime lineupAnnounceTime = kickOffTime.minusHours(LINEUP_ANNOUNCE_BEFORE_HOUR);

        startLineupJobSchedulerService.addJob(fixtureId, lineupAnnounceTime);
        liveFixtureJobSchedulerService.addJob(fixtureId, kickOffTime);

        log.info("Fixture jobs added for fixtureId={}", fixtureId);
    }

    public void removeFixtureJobs(Fixture fixture) throws SchedulerException {
        long fixtureId = fixture.getFixtureId();

        // 잡 삭제
        startLineupJobSchedulerService.removeJob(fixtureId);
        liveFixtureJobSchedulerService.removeJob(fixtureId);
        liveFixtureJobSchedulerService.removePostJob(fixtureId);

        log.info("Fixture jobs removed for fixtureId={}", fixtureId);
        fixture.setAvailable(false);
    }

    private ZonedDateTime toSeoulZonedDateTime(LocalDateTime kickoffTime, String timeZone, long timestamp) {
        try {
            ZoneId zoneId = ZoneId.of(timeZone);
            return ZonedDateTime.of(kickoffTime, zoneId)
                    .withZoneSameInstant(ZoneId.of("Asia/Seoul"));
        } catch (ZoneRulesException e) {
            Instant instant = Instant.ofEpochSecond(timestamp);
            return ZonedDateTime.ofInstant(instant, ZoneId.of("Asia/Seoul"));
        }
    }
}
