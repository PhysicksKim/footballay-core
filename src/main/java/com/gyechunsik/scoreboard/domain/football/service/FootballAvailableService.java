package com.gyechunsik.scoreboard.domain.football.service;

import com.gyechunsik.scoreboard.domain.football.entity.Fixture;
import com.gyechunsik.scoreboard.domain.football.entity.League;
import com.gyechunsik.scoreboard.domain.football.repository.FixtureRepository;
import com.gyechunsik.scoreboard.domain.football.repository.LeagueRepository;
import com.gyechunsik.scoreboard.domain.football.scheduler.lineup.StartLineupJobSchedulerService;
import com.gyechunsik.scoreboard.domain.football.scheduler.live.LiveFixtureJobSchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.SchedulerException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
    private final LiveFixtureJobSchedulerService liveFixtureJobSchedulerService;

    private final static int LINEUP_ANNOUNCE_BEFORE_HOUR = 1;

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
        log.info("Add Scheduler Job :: fixtureId={}, kickoffTime={}, lineupAnnounceTime={}",
                fixtureId, kickOffTime, lineupAnnounceTime);
        startLineupJobSchedulerService.addJob(fixtureId, lineupAnnounceTime);
        liveFixtureJobSchedulerService.addJob(fixtureId, kickOffTime);
        fixture.setAvailable(true);
        fixtureRepository.save(fixture);
    }

    public void removeAvailableFixture(long fixtureId) throws SchedulerException {
        log.info("removeAvailableFixture :: fixtureId={}", fixtureId);
        Fixture fixture = fixtureRepository.findById(fixtureId)
                .orElseThrow(() -> new IllegalArgumentException("fixture not found"));

        log.info("Remove Scheduler Job :: fixtureId={}", fixtureId);
        startLineupJobSchedulerService.removeJob(fixtureId);
        liveFixtureJobSchedulerService.removeJob(fixtureId);
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
    public List<Fixture> findClosestFixturesFromDate(long leagueId, ZonedDateTime matchDateFrom) {
        ZonedDateTime truncated = matchDateFrom.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime localDateTime = truncated.toLocalDateTime();
        League league = leagueRepository.findById(leagueId).orElseThrow();
        log.info("findClosestFixturesFromDate :: leagueId={}, matchDateFrom={}", leagueId, matchDateFrom);
        log.info("truncated time zoned={} local={}", truncated, localDateTime);

        // Find the first fixture after the given date
        fixtureRepository.findAvailableFixturesByLeagueAndDate(league, localDateTime);
        List<Fixture> fixturesByLeagueAndDate = fixtureRepository.findFixturesByLeagueAndDateAfter(
                league,
                localDateTime,
                getClosestFixturePageRequest()
        );

        if(fixturesByLeagueAndDate.isEmpty()) {
            return List.of();
        }

        // Get the date of the closest fixture
        Fixture closestFixture = fixturesByLeagueAndDate.get(0);
        LocalDateTime closestDate = closestFixture.getDate().truncatedTo(ChronoUnit.DAYS);
        List<Fixture> closestDateFixtures = fixtureRepository.findFixturesByLeagueAndDateRange(
                league,
                closestDate,
                closestDate.plusDays(1).minusSeconds(1)
        );
        log.info("closestDateFixtures={}", closestDateFixtures);
        return closestDateFixtures;
    }

    // TODO : findFixturesByDate(), findClosestAvailableFixturesFromDate(), findAvailableFixturesByDate() 3가지 구현해야함

    private static Pageable getClosestFixturePageRequest() {
        return PageRequest.of(0, 1, Sort.by(Sort.Direction.ASC, "date"));
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
