package com.gyechunsik.scoreboard.domain.football.service;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.scheduler.lineup.StartLineupJobSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.zone.ZoneRulesException;
import java.util.List;

@Slf4j
@Transactional
@RequiredArgsConstructor
@Service
public class FootballAvailableService {

    private final LeagueRepository leagueRepository;
    private final FixtureRepository fixtureRepository;
    private final StartLineupJobSchedulerService startLineupJobSchedulerService;

    private final int LINEUP_ANNOUNCE_BEFORE_HOUR = 1;

    public void updateAvailableLeague(long leagueId, boolean isAvailable) {
        log.info("updateAvailableLeague :: leagueId={}, isAvailable={}", leagueId, isAvailable);
        League league = leagueRepository.findById(leagueId).orElseThrow();
        league.setAvailable(isAvailable);
        leagueRepository.save(league);
    }

    public List<League> getAvailableLeagues() {
        log.info("getAvailableLeagues");
        List<League> leagues = leagueRepository.findAllByAvailableOrderByCreatedDateDesc(true);
        log.info("leagues={}", leagues);
        return leagues;
    }

    public void addAvailableFixture(long fixtureId) throws SchedulerException {
        log.info("addAvailableFixture :: fixtureId={}", fixtureId);
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("fixture not found"));

        ZonedDateTime kickOffTime = toSeoulZonedDateTime(
                fixture.getDate(), fixture.getTimezone(), fixture.getTimestamp()
        );
        // TODO : 향후 summer time 적용된 리그인지 체크해서 서머타임 보정 해야함
        ZonedDateTime lineupAnnounceTime = kickOffTime.minusHours(LINEUP_ANNOUNCE_BEFORE_HOUR);
        log.info("try to add LineupJob schedule of fixtureId={} Start At lineupAnnounceTime={}",
                fixtureId, lineupAnnounceTime);
        startLineupJobSchedulerService.addJob(fixtureId, lineupAnnounceTime);
        fixture.setAvailable(true);
        fixtureRepository.save(fixture);
    }

    public void removeAvailableFixture(long fixtureId) throws SchedulerException {
        log.info("removeAvailableFixture :: fixtureId={}", fixtureId);
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("fixture not found"));

        startLineupJobSchedulerService.removeJob(fixtureId);
        fixture.setAvailable(false);
        fixtureRepository.save(fixture);
    }

    /**
     * 주어진 date 를 포함하여, 가장 가까운 fixture 가 있는 날짜의 모든 fixture 를 반환합니다.  <br>
     * 예를 들어 2024/06/13 에 최초로 fixture 가 2개 있고, 2024/06/14 에 fixture 가 3개 있다면  <br>
     * 1) 2024/06/01 로 요청하는 경우 2024/06/13 에 있는 2개의 fixture 를 제공합니다.  <br>
     * 2) 2024/06/13 의 아무 시간 으로 요청하는 경우 2024/06/13 에 있는 2개의 fixture 를 제공합니다 <br>
     * @param leagueId 리그 아이디
     * @param matchDateFrom 포함하여 가장 가까운 날짜에 있는 모든 경기일정 조회
     * @return 해당 날짜에 있는 모든 경기일정
     */
    public List<Fixture> getAvailableFixturesFromDate(long leagueId, ZonedDateTime matchDateFrom) {
        log.info("getAvailableFixturesFromDate :: leagueId={}, matchDateFrom={}", leagueId, matchDateFrom);
        ZonedDateTime truncated = matchDateFrom.truncatedTo(ChronoUnit.DAYS);
        return fixtureRepository.findAvailableFixturesByLeagueIdAndDate(leagueId, truncated);
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
